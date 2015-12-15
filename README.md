# d-fence

A Clojure application - which runs as a reverse proxy - designed to serve as a protection layer of an API service backend. It does so by using security access matrix configured by users or developers while making use of jwt token containing claims about API consumers.

## Usage
Author security access rules in the form of a matrix specified as config files used by the application, listening on an HTTP/S port, serving the API consumer client.
e.g.
| Method | URI      | is-service | role1 | role2 |
| GET    | /detail  | X          | X     |       |
| POST   | /add     | X          |       | X     |

## License

Copyright © 2015 Alfred Xiao

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
