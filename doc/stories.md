Stories:
[DONE] 0. can start and stop web server, and server hello world, have capability to read config
[DONE] 1. can reverse-proxy GET
[DONE] 2. can reverse-proxy ANY method
[DONE] 3. can collect incoming request facts (protocol, method, uri, ip, time, date, weekday)
[DONE] 4. parse rules with primitives (e.g. has-valid-token, is-service)
[DONE] 5. evaluate rules with regard to primitives (comparing those in rules and those from facts)
[DONE] 6. fix bug where we replace Location only when it matches backend host/port
[DONE] 7. remove dependency on cemerick.url
normalise condition name (where you have role name like STAFF_ROLE)

5. default rule (to deny if no rules are defined)
9. can parse a rule with facts like IP, weekday/time
11. allow enumeration of method names
12. allow enumeration of uri parts (e.g. /api/rollovers/{in|out})
18. have an endpoint to display current rules/config