# Agile Guidelines

Internal development rules for LCDCI team working in a Sprint-based Scrum environment.


## Definition of Ready (DoR)

A Product Backlog Item (PBI) is **READY** when **all** of the following conditions are met:

1. **Clear Business Value**  
   The PBI explains why this feature matters to the client or system.

2. **Acceptance Criteria Defined**  
   Testable, unambiguous acceptance criteria are written and linked in Jira.

3. **No Major Blockers**  
   All dependencies are resolved or mocked. The team can start immediately.

4. **Sized Appropriately**  
   The ticket can be completed within one sprint.  
   Estimates must be realistic (±2 hours margin).  
   PBIs too large must be split before sprint planning.

5. **Technical Requirements Clarified**  
   APIs, payloads, database fields, or UI expectations are known and documented.

6. **Acceptance Tests Identified**  
   QA/Developer knows exactly what to verify to confirm completion.

7. **Prioritized**  
   The PBI has a clear priority in the Sprint Backlog.


## Definition of Done (DoD)

A PBI is **DONE** when **all** the following are true:

1. **Implementation Complete**  
   All acceptance criteria are fully met with no placeholder code.

2. **Code Reviewed & Approved**  
   PR created following naming conventions.  
   At least one team member approved the PR.  
   Impacted areas reviewed when necessary.

3. **Quality Standards Met**  
   Code compiles and runs without errors.  
   No console/server errors.  
   No unused code or commented-out blocks.  
   Follows project conventions and folder structure.

4. **Tests Performed**  
   Manual or unit tests executed.  
   Expected behaviour verified.

5. **No Regression**  
   Existing features remain functional.  
   No unexpected breaking changes.

6. **Documentation Updated**  
   API docs, interface schemas, or relevant README sections updated if needed.  
   Jira ticket updated and transitioned to “Done”.

7. **Merged into Main**  
   The PR is merged using **Squash and Merge**, and the branch is deleted.