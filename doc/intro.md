# Introduction to dfence

Concepts:
- Backend Service (any better name?): The backend microservice being protected by dfence
- Rule: Declaring whether to grant or deny access based on known facts
 = Example:
  # Grant GET /version to everyone
  # Grant POST /find-member-registrations to role:MROR
- Facts: Assertions about current situation
 = Endpoint Facts: HTTP method and target URI
 = Request Facts: facts about incoming requests and user token
 = Data Facts: facts fetched from an external source (e.g. an external microservice)

How It Works
- In a request evaluation process, rules are evaluated in order, once RESOLVED (to either Grant or Deny), rule evaluation stops.
- If no rules are RESOLVED, defaults to specified 'Default'.
- If no 'Default' is specified, dfence assumes 'Deny'
- Implementation wide, Method/URI will be matched first and rule out those rules irrelevant

Features
- Logical AND and OR
- Enumeration
 = on method or uri, or rolename
- Wild Card (No Regular Expression!)
 = on rule fields
  # HTTP Method
  # URI
  # rolename
  # username
 = Examples
  # Grant PUT /api/rollover/*/ to role:ROLLOVERS
  # Grant * /api/* to role:APPROVER
- URI Destructuring
  # Grant PUT /add-response/{staff-flag} to role:MROR_{staff_flag}
- Data Facts
  # Grant POST /find-member-detail/{id} to role:MROR_STAFF and is-staff-member:true

Built-in Facts
- http-method
- protocol
- uri
- username
- role
- ip
- time
- date
- weekday
- header['header-name']

Data Facts (Secretary/Assistant to serve read-only metadata)
is-staff-member: http://localhost:9000/is-staff-member/{id}  returns true
