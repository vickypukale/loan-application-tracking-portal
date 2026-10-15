# Target Node Configuration Specification
## Loan Application Tracking Portal Server Baseline

---

## 1. Operating System & Target Platform
* **Operating System:** Ubuntu 22.04 LTS (Jammy Jellyfish) / Debian 12 / Linux Target Node (or WSL2 on developer workstation).
* **Architecture:** x86_64 / amd64.

---

## 2. Package Prerequisites
| Package Name | Purpose | Minimum Version |
| :--- | :--- | :--- |
| `curl`, `wget`, `gnupg` | Package repository management and network utilities | Latest |
| `openjdk-17-jre-headless` | Java 17 Runtime Environment for standalone execution | 17.0+ |
| `docker.io` / `docker-ce` | Container runtime engine for isolated deployment | 24.0+ |
| `docker-compose-plugin` | Multi-container orchestration utility | 2.20+ |
| `ufw` | Host-based firewall management | Latest |
| `net-tools`, `procps` | Process monitoring and port verification | Latest |

---

## 3. User & Group Matrix
| User / Group | UID / GID | Shell | Description & Access |
| :--- | :--- | :--- | :--- |
| `loanapp` (User) | `1001` | `/bin/bash` | Dedicated unprivileged service account for running portal processes. |
| `loanapp` (Group) | `1001` | N/A | Primary group for `loanapp` user. |
| `docker` (Group) | System | N/A | Added to `loanapp` user to permit rootless container execution. |

---

## 4. Directory & File Permission Matrix
| Path | Type | Owner:Group | Mode | Purpose |
| :--- | :--- | :--- | :--- | :--- |
| `/opt/loan-portal` | Directory | `loanapp:loanapp` | `0755` | Root application deployment directory. |
| `/opt/loan-portal/logs` | Directory | `loanapp:loanapp` | `0755` | Application log rotation storage. |
| `/opt/loan-portal/config` | Directory | `loanapp:loanapp` | `0750` | Application environment and properties files. |
| `/opt/loan-portal/data` | Directory | `loanapp:loanapp` | `0750` | Local database / document storage vault. |
| `/opt/loan-portal/.env` | File | `loanapp:loanapp` | `0640` | Port and environment runtime variables. |

---

## 5. Network Ports & Firewall Rules (UFW)
| Port | Protocol | Inbound / Outbound | Source | Purpose |
| :--- | :--- | :--- | :--- | :--- |
| `22` | TCP | Inbound | Authorized Bastion / All | Secure Shell (SSH) remote management |
| `8081` | TCP | Inbound | Any | Loan Application Tracking Portal web UI & Actuator |
| `Any` | All | Outbound | Target Node | HTTPS / Package updates / Image pulling |

---

## 6. System Services
* `docker.service`: Must be enabled on boot and in an active `running` state.
* `ufw.service`: Enabled and enforcing inbound firewall rules.
