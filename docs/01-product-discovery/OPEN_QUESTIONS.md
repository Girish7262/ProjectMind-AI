# Open Questions

## Document Metadata

| Metadata Field | Value |
|---|---|
| Document Type | Product Discovery / Open Questions |
| Version | 1.0.0 |
| Status | Published |
| Owner | Portfolio Developer |
| Reviewer | Portfolio Developer |
| Last Updated | 2026-07-22 |

---

## Executive Summary

Product Discovery is an iterative process that uncovers functional, technical, operational, and compliance uncertainties. Maintaining an active registry of open questions is critical for risk management and governance, ensuring that assumptions requiring validation are tracked rather than overlooked.

This document logs unresolved questions, external dependencies, and pending decisions identified during discovery. By categorizing and prioritizing these queries, this document provides a roadmap for research and validation to guide the subsequent Product Vision and Requirements Engineering phases.

---

## Business Questions

* **Customer Segment Prioritization:** Which enterprise customer segments (e.g., mid-market SaaS companies vs. regulated Fortune 500 financial institutions) should be prioritized for early pilot validation?
* **Pricing and Licensing Model:** What licensing model (e.g., per-active-seat, repository-volume pricing, or consumption-based query pricing) best aligns with enterprise procurement cycles?
* **Deployment Topology Support:** Which deployment models (multi-tenant SaaS, single-tenant private cloud VPC, or fully on-premise air-gapped instances) must be supported to clear enterprise software sales barriers?
* **Adoption and Enablement Strategy:** What adoption strategy (e.g., top-down management mandate vs. bottom-up developer-led organic advocacy) will maximize user engagement during corporate rollouts?

---

## Product Questions

* **MVP Scope Boundary:** What core search and retrieval capabilities are mandatory for the Minimum Viable Product (MVP) vs. features deferred to later releases?
* **Future Release Roadmap:** Which secondary features (such as automated documentation generation, cross-repository dependency visualization, or automated translation of comments) belong to the long-term roadmap?
* **Confidence Representation:** How should semantic query confidence and source citations (e.g., linking answers to specific files, closed tickets, or wiki edits) be displayed to build developer trust?
* **AI Explainability Requirements:** What level of AI reasoning and source lineage representation is required to satisfy enterprise audit and compliance administrators?

---

## Technical Questions

* **LLM Selection and Ingestion:** Which Large Language Models (LLMs) (e.g., secure cloud-hosted enterprise APIs vs. open-source models like Llama-3 hosted locally within the client VPC) should be supported?
* **Semantic Indexing Strategy:** How should heterogeneous data sources (structured code files, semi-structured Jira tickets, and unstructured Confluence wikis) be parsed and mapped into a unified semantic knowledge graph?
* **Data Ingestion Approach:** Should the platform rely on pull-based scheduled API polling, push-based webhook triggers, or direct database replication connectors to ingest changes?
* **Scalability Targets:** What are the exact performance limits (e.g., maximum codebase line count, historical ticket thresholds, and concurrent user query volume) that the backend architecture must support?

---

## Security & Compliance Questions

* **Enterprise SSO Integrations:** What authentication mechanisms (SAML 2.0, Okta, Active Directory, or multi-factor OAuth) must be supported out-of-the-box?
* **Access Boundary Protection:** How will the platform prevent unauthorized users from retrieving insights from sensitive, access-controlled code repositories or private Jira projects?
* **Activity Audit Logging:** What audit logging requirements exist for tracking user searches, source metadata indexing changes, and API configuration adjustments?
* **Industry Compliance Certifications:** What compliance frameworks (e.g., SOC 2 Type II, ISO 27001, HIPAA, or GDPR) must be achieved prior to general enterprise deployment?

---

## Operational Questions

* **Knowledge Ownership Management:** How will system access changes be handled if a user's permissions in GitHub diverge from their permissions in Confluence?
* **Grounded Answer Validation:** Who is responsible for validating generated query answers if discrepancies or contradictions occur between outdated Confluence pages and active codebase files?
* **Semantic Indexing Frequency:** How frequently should indexing pipelines run (e.g., near-real-time commit webhooks vs. nightly batch indexing) to balance hosting cost and index freshness?
* **Outdated Context Remediation:** How should the indexing pipeline identify, flag, or ignore deprecated source documentation and retired code segments to prevent returning stale answers?

---

## External Dependencies

The planning and execution of ProjectMind AI rely on the following external dependencies, which represent risk factors:

* **Third-Party APIs:** Ingestion performance is dependent on the availability, response time, and rate limits of GitHub, Jira, and Confluence REST/GraphQL APIs.
* **Enterprise Identity Providers:** Configuration of single sign-on (SSO) depends on the client organization's IT team providing SAML or Okta directory access.
* **Hosting and Infrastructure Providers:** Processing speeds are bound to the hosting cost and availability of GPU instances and scalable vector search databases.
* **AI Model Providers:** Reliability is linked to the performance, SLA commitments, and security contracts of selected corporate LLM vendors or hosted private instances.
* **Baseline Documentation Quality:** The semantic accuracy of search answers depends on the baseline existence of code comments, Jira summaries, and wiki files within the client's environment.

---

## Decision Log

The following matrix tracks the status of critical decision areas that must be resolved prior to development:

| Decision Area | Current Status | Designated Owner | Target Resolution Date |
|---|---|---|---|
| **MVP Capability Boundaries** | Under Review | Product Manager | 2026-08-15 |
| **LLM Deployment Model (In-VPC vs. Cloud API)** | Pending Architecture Review | Developer | 2026-08-20 |
| **Role-Based Access Control (RBAC) Mapping** | Pending Security Audit | Technical Lead | 2026-08-30 |
| **Initial Pilot Target Segments** | Under Evaluation | Developer | 2026-08-10 |
| **Subscription Pricing Structure** | Under Review | Developer | 2026-09-05 |

---

## Prioritized Open Questions

The following prioritized registry lists open questions based on their business impact and urgency:

| Open Question | Category | Business Impact | Action Priority |
|---|---|---|---|
| How do we enforce Role-Based Access Control (RBAC) so users only retrieve info they are authorized to see? | Security & Compliance | High (Critical for security clearance) | Critical |
| Which LLM hosting model provides the best balance between codebase privacy and parsing accuracy? | Technical | High (Core adoption gate) | Critical |
| What core features are required for the initial MVP vs. future releases? | Product | Medium (Determines release timeline) | High |
| How do we handle API rate limits during the initial indexing of large codebases? | Technical | Medium (Controls scalability limits) | High |
| How should the platform present query confidence and citations to the developer? | Product | Medium (Determines user trust) | High |
| What pricing model aligns best with enterprise budget cycles? | Business | Medium (Influences revenue growth) | Medium |
| What batch frequency is optimal for semantic indexing runs? | Operational | Low (Determines hosting overhead) | Medium |

---

## Next Steps

To resolve these open questions systematically, specific deliverables will be prioritized during subsequent project phases:

* **Product Vision:** Refine target market definitions and pricing parameters, aligning early sales initiatives with prioritized customer segments.
* **Business Requirements (BRD):** Document operational agreements, target SLAs, compliance certifications, and security audit checklists.
* **Functional Requirements (FRD):** Detail specific user stories, IDE interfaces, search behaviors, and access-control validation steps to define the MVP scope.
* **Architecture Design:** Define database models, LLM ingestion pipelines, index scaling plans, and RBAC mapping layers to resolve rate-limiting and performance blockers.

---

## Conclusion

Maintaining a registry of open questions is a critical risk mitigation step for ProjectMind AI. Defining the boundaries of the platform ensures that system requirements remain realistic, security policies are respected, and the R&D team builds a robust knowledge continuity layer that operates safely alongside the enterprise's existing developer combo.

---

## Revision History

| Version | Date | Author / Role | Summary of Changes |
|---|---|---|---|
| 0.1.0 | 2026-07-22 | Developer / Architect | Initial creation of the Open Questions discovery document. |
