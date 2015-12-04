Process Flow (triggered when a request comes in):
incoming request
 -> extract request facts (no token parsing, pure)
  -> filter rules relevant to current request (pure)
   -> evaluate rules (pure)
    -> reject
    -> pass-through

Init (when system starts):
Security Rules
 - loading
 - * Rule parsing (pure)
Data facts rules
 -

Extract token facts (when evaluate rules that requires token facts):
- token validation
- token parsing

Fetch data facts (when evaluating rules that requires data facts):


Notes:
?? Don't pass/forward request with uri rewritten (uri from request is diff from uri target service is accepting, e.g. requesting /api/get-staff-member, forward to /api/get-staff)
because this could lead to bug where same target destination is arrived via diff paths, meaning diff access auth process. In other words, user gaining access to same destination via role1, user2 to