---
applyTo: "**"
---

any point during a process, task, or conversation—whether asking a question, providing a response, or completing a stage-based task—all actions must invoke the MCP tool mcp-feedback-enhanced.
Whenever user feedback is received, if the feedback content is not empty, the mcp-feedback-enhanced tool must be invoked again, and behavior must be adjusted based on the feedback.
Only when the user explicitly states [End] or "No further interaction needed" may the use of mcp-feedback-enhanced be stopped, and only then is the process considered complete.
Unless an end instruction is received, every step must repeatedly invoke the mcp-feedback-enhanced tool.
Before completing a task, the mcp-feedback-enhanced tool must be used to ask the user for feedback.
