# LMS Microservices

A microservices-based Learning & Management System built with Spring Boot and H2, designed as a vehicle for practicing Kubernetes, DevSecOps, and CI/CD (M.Tech final year project).

## Architecture

```
                              ┌─────────────────────┐
                     JWT      │                      │
   Client  ───────────────►  │     api-gateway      │  :8080
                              │  (Spring Cloud       │
                              │   Gateway + JWT       │
                              │   perimeter auth)     │
                              └──────────┬───────────┘
                                         │ routes by path, forwards
                                         │ X-User-Id / X-User-Role headers
              ┌───────────────┬─────────┼─────────┬───────────────┐
              ▼               ▼         ▼         ▼               ▼
      ┌───────────────┐┌─────────────┐┌────────────────┐┌──────────────────┐┌──────────────────────┐
      │ auth-service  ││student-svc  ││ course-service  ││ assignment-svc    ││ notification-service  │
      │    :8081      ││   :8082     ││     :8083       ││      :8084        ││        :8085          │
      │ users, JWT    ││ profiles,   ││ courses,        ││ assignments,      ││ in-app + email-stub    │
      │ issuance      ││ enrollments ││ modules         ││ submissions/grades││ notifications          │
      └──────┬────────┘└──────┬──────┘└───────┬─────────┘└─────────┬─────────┘└──────────┬─────────────┘
             ▼                ▼                ▼                   ▼                     ▼
         [H2: authdb]   [H2: studentdb]   [H2: coursedb]    [H2: assignmentdb]     [H2: notificationdb]
```

Each service owns its own H2 database file (database-per-service) — no shared schema, no cross-service SQL joins. Services reference each other only by numeric ID (e.g. `Enrollment.courseId` refers to a `Course.id` in course-service) and would call each other's REST APIs if a workflow ever needs to enrich data across services. The gateway is the single public entry point; it validates the JWT once at the perimeter and forwards the caller's identity downstream via `X-User-Id` / `X-Username` / `X-User-Role` headers, so backend services don't need to re-verify signatures.

## Services & default ports

| Service | Port | Responsibility |
|---|---|---|
| api-gateway | 8080 | Routing + JWT perimeter auth |
| auth-service | 8081 | Registration, login, JWT issuance/validation |
| student-service | 8082 | Student profiles, course enrollment & progress |
| course-service | 8083 | Course catalog, modules |
| assignment-service | 8084 | Assignments, submissions, grading |
| notification-service | 8085 | In-app notifications + email stub |

All requests should go through the gateway at `:8080` — the individual service ports are exposed only for local debugging and the H2 console (`/h2-console` on each service).

## Key API endpoints (via gateway, base `http://localhost:8080`)

- `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/me`
- `POST /api/students`, `GET /api/students/{id}`
- `POST /api/enrollments`, `PATCH /api/enrollments/{id}/progress`
- `POST /api/courses`, `GET /api/courses/{id}/details`, `PATCH /api/courses/{id}/publish`
- `POST /api/modules`, `GET /api/modules/course/{courseId}`
- `POST /api/assignments`, `POST /api/submissions`, `PATCH /api/submissions/{id}/grade`
- `POST /api/notifications`, `GET /api/notifications/user/{userId}`

`POST /api/auth/register` and `POST /api/auth/login` are the only endpoints that don't require a bearer token; everything else needs `Authorization: Bearer <token>` from `/login`.

## Running locally (no Docker)

Each module is a standalone Spring Boot app. Open 6 terminals (or run in the background) from the project root:

```bash
cd auth-service && mvn spring-boot:run &
cd student-service && mvn spring-boot:run &
cd course-service && mvn spring-boot:run &
cd assignment-service && mvn spring-boot:run &
cd notification-service && mvn spring-boot:run &
cd api-gateway && mvn spring-boot:run &
```

Or build once and run the jars:

```bash
mvn clean package -DskipTests
java -jar auth-service/target/auth-service.jar &
java -jar student-service/target/student-service.jar &
java -jar course-service/target/course-service.jar &
java -jar assignment-service/target/assignment-service.jar &
java -jar notification-service/target/notification-service.jar &
java -jar api-gateway/target/api-gateway.jar &
```

> This sandbox environment has no internet access to Maven Central, so the build could not be compiled or run here. Everything was hand-written and reviewed for consistent packages/imports/signatures — run `mvn clean verify` on a machine with normal internet access to confirm.

## Running with Docker Compose

```bash
docker compose build
docker compose up -d
docker compose logs -f api-gateway
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"jdoe","email":"jdoe@example.com","password":"Passw0rd!","role":"STUDENT"}'
```

Each service persists its H2 file database to a named Docker volume, so data survives container restarts.

## Running on Kubernetes

Manifests live in `k8s/` and are wired together with Kustomize.

```bash
# Build images locally first (or let CI push to a registry and update k8s/kustomization.yaml image tags)
docker build -f auth-service/Dockerfile -t lms/auth-service:1.0.0 .
docker build -f student-service/Dockerfile -t lms/student-service:1.0.0 .
docker build -f course-service/Dockerfile -t lms/course-service:1.0.0 .
docker build -f assignment-service/Dockerfile -t lms/assignment-service:1.0.0 .
docker build -f notification-service/Dockerfile -t lms/notification-service:1.0.0 .
docker build -f api-gateway/Dockerfile -t lms/api-gateway:1.0.0 .

# If using minikube/kind, load images into the cluster's local image store, e.g.:
minikube image load lms/auth-service:1.0.0   # repeat per image

kubectl apply -k k8s/
kubectl -n lms get pods -w
```

Then either port-forward the gateway or use the included Ingress (`k8s/21-ingress.yaml`, requires ingress-nginx):

```bash
kubectl -n lms port-forward svc/api-gateway 8080:8080
# or, with Ingress + /etc/hosts entry for lms.local:
curl http://lms.local/api/auth/health
```

What's included for a production-shaped deployment:
- **Namespace isolation** (`00-namespace.yaml`)
- **Secrets** for the shared JWT signing key (`01-secret.yaml`) — replace with a real secret manager (Vault, Sealed Secrets, SOPS) before this ever ships
- **ConfigMap** for service discovery URLs and shared config (`02-configmap.yaml`)
- **PersistentVolumeClaims** per service (H2 file DB needs a durable volume; `strategy: Recreate` on each Deployment avoids two pods writing to the same H2 file at once)
- **Liveness/readiness probes** against Spring Boot Actuator's `/actuator/health`
- **Resource requests/limits** so the scheduler and cluster autoscaler behave sensibly
- **NetworkPolicies** (`30-network-policy.yaml`) — default-deny ingress, then explicit allow rules so backend services are only reachable from the gateway, not from arbitrary pods or the outside world
- **HorizontalPodAutoscaler** on the gateway (`20-api-gateway.yaml`)
- Pods run as non-root with dropped Linux capabilities

## CI/CD & DevSecOps

Two equivalent pipelines are provided — use whichever your grading rubric or infra expects:

- **GitHub Actions** — `.github/workflows/ci-cd.yml`
- **Jenkins** — `Jenkinsfile`

Both follow the same stages:

1. **Build & unit test** every module (Maven, JUnit 5 + Mockito, JaCoCo coverage)
2. **SAST** — SonarQube/SonarCloud static analysis (code smells, bugs, security hotspots)
3. **SCA** — Trivy filesystem scan over dependencies and IaC/YAML for known CVEs and misconfigurations
4. **Build container images**, then **scan the images themselves** with Trivy (fails the pipeline on HIGH/CRITICAL, unfixed vulnerabilities are ignored so the build isn't blocked on issues with no available patch)
5. **Push** to a container registry (GHCR in the sample workflow) — main branch only
6. **Deploy** to Kubernetes via `kustomize`/`kubectl apply -k` — main branch only, gated behind a `production` environment in GitHub Actions

To enable the pipelines you'll need to configure, per platform:
- GitHub Actions: repo secrets `SONAR_TOKEN`, `SONAR_HOST_URL`, `KUBE_CONFIG` (base64 kubeconfig); repo variable `SONAR_ENABLED=true` to turn on the Sonar stage
- Jenkins: credentials `sonarqube-token`, `registry-credentials`, `kubeconfig-lms`; global tools `JDK11`, `Maven3`

## Security notes / things to harden before this is "real"

- The JWT secret is a literal string in `application.yml`/`docker-compose.yml`/`k8s/01-secret.yaml` for demo convenience — in any shared or production environment, generate a random secret and inject it via your secret manager, never commit it to git.
- Backend services currently trust the `X-User-*` headers set by the gateway without re-verifying anything — this is the standard "perimeter auth" pattern, but it means backend services must never be reachable except through the gateway. The included `NetworkPolicy` enforces that at the cluster level.
- `auth-service`'s own `/api/auth/me` endpoint does independently re-validate the JWT (see `JwtAuthFilter`), as a second line of defense on that one service.
- H2 is intentionally used here as a lightweight, zero-ops database per the project requirements. For anything beyond a demo/coursework deployment, swap it for a managed Postgres/MySQL per service.

## Repository layout

```
lms-project/
├── pom.xml                      # parent (dependency management, shared build config)
├── auth-service/
├── student-service/
├── course-service/
├── assignment-service/
├── notification-service/
├── api-gateway/
├── docker-compose.yml
├── Jenkinsfile
├── .github/workflows/ci-cd.yml
└── k8s/                         # namespace, secret, configmap, per-service Deployment/Service/PVC,
                                  # gateway + HPA, Ingress, NetworkPolicy, kustomization.yaml
```
