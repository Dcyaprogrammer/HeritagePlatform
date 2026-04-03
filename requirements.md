# Community Heritage Resource Sharing & Curation Platform

## 1. Project Overview
Develop a web-based community heritage resource sharing and curation platform for submitting, reviewing, publishing, and archiving curated resource entries. 

The platform focuses on community heritage and local culture topics (e.g., places, traditions, stories, objects, or educational materials).

## 2. Main User Roles
* **Administrator / Reviewer:** Manages users, master data, and reviews submitted resources.
* **Contributor:** Creates and submits heritage resources for review.
* **Registered Viewer:** Browses, searches, and interacts with approved resources.

## 3. Core Functional Requirements

### A. User and Access Management
* User registration, login, and logout.
* Profile maintenance (avatar, bio, etc.).
* Contributor approval by the administrator.

### B. Resource Submission and Management
* Contributors can create, edit, and submit resources with metadata:
  * Title
  * Category / Topic
  * Place (Location)
  * Description
  * Tags / Keywords
  * File upload and/or external link (Attachments)
  * Copyright / Usage declaration
* Contributors can revise and resubmit rejected resources.

### C. Review and Publication Workflow
* Resource status management (minimum statuses): 
  * `DRAFT`
  * `PENDING_REVIEW`
  * `APPROVED`
  * `REJECTED`
  * `ARCHIVED`
* Reviewer approval or rejection with feedback/comments.

### D. Discovery and Use
* Browse, search, and filter approved resources.
* View detailed information about a resource.
* Basic commenting and feedback on approved resources.

### E. Administration and Reporting
* Manage categories and tags (Master Data).
* Archive or unpublish resources.

## 4. Business Rules (Minimum)
* **Rule 1:** Only approved contributors may submit resources.
* **Rule 2:** Only approved resources (`APPROVED` status) are visible to general users.
* **Rule 3:** Archived resources (`ARCHIVED` status) are hidden from general users.
