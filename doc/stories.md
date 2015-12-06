Stories:
[DONE] 0. can start and stop web server, and server hello world, have capability to read config
[DONE] 1. can reverse-proxy GET
[DONE] 2. can reverse-proxy ANY method
[DONE] 3. can collect incoming request facts (protocol, method, uri, ip, time, date, weekday)
4. can collect Request facts from token (username, role)
5. default rule (to deny if no rules are defined)
6. can parse a default rule (default to deny or grant as specified/configured)
7. can parse a rule with role fact
8. can parse a rule with username fact
9. can parse a rule with ip fact
10. can parse a rule with weekday/time fact
11. allow enumeration of method names
12. allow enumeration of uri parts (e.g. /api/rollovers/{in|out})
13. allow wild card
14. URI destructuring, i.e. creating new facts, e.g. /find-member-detail/{id} => new fact id:223
15. can specified data facts
16. can create data facts
17. allow data facts to be used in rules