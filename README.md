# Mantis API

API Java Spring simulant une API MantisBT sur la version 1.2.17 à l'aide de Selenium.

## Endpoints

Les endpoints sont largement inspirés par l'[API des versions supérieures de MantisBT](https://documenter.getpostman.com/view/29959/mantis-bug-tracker-rest-api/7Lt6zkP#16677869-82ac-50a0-e69f-c33986fbcf5f)

| catégorie | nom                       | fonction                                              | schema                                  | params (default value)                                                                                                    |
|-----------|---------------------------|-------------------------------------------------------|-----------------------------------------|---------------------------------------------------------------------------------------------------------------------------|
| issues    | `Get` an issue            | Get issue with the specified id.                      | **/api/rest/issues**/:issue_id          | RP: specific fields : select                                                                                              |
| issues    | `Get` all issues          | Get paginated list of issues.                         | **/api/rest/issues**                    | RP: page_size (50) / page (1) / select / project_id /<br/>filtre (*ex: 1, 2, reported, assigned, unassigned*) : filter_id |
| issues    | `Post` an issue           | Create an issue                                       | **/api/rest/issues**                    |                                                                                                                           |
| issues    | Modify an issue (`Post`)  | Change status...                                      |                                         |                                                                                                                           |
| issues    | `Delete` an issue         | Deletes an issue given its id.                        | **/api/rest/issues**/:issue_id          |                                                                                                                           |
| issues    | Monitor an issue (`Post`) | Have logged in user monitor the specified issue.      | **/api/rest/issues**/:issue_id/monitors | Body: list of _users_ : user who monitors                                                                                 |
| issues    | Monitor tags (`Post`)     |                                                       | **/api/rest/issues**/:issue_id/tags     | Body: Attach tags : list of _tags_ / Detach tag : <br/><br/><br/>_tag_id_ <br/> <br/> <br/>  <br/><br/>                   |
|           |                           |                                                       |                                         |                                                                                                                           |
| projects  | `Get` a project           | Get a project given its id.                           | **/api/rest/projects**/:project_id      |                                                                                                                           |
| projects  | `Get` all projects        | Get all projects and sub-projects accessible to user. | **/api/rest/projects**                  |                                                                                                                           |
| projects  | `Post` a project          | Create a project.                                     | **/api/rest/projects**                  |                                                                                                                           |
| projects  | `Patch` a project         | Update a project.                                     | **/api/rest/projects**/:project_id      | Body: content to modify                                                                                                   |
| projects  | `Delete` a project        | Delete a project.                                     | **/api/rest/projects**/:project_id      |                                                                                                                           |
|           |                           |                                                       |                                         |                                                                                                                           |

## Auteurs:

> Ylane TURAM-ULIEN
> 
> Edwin VIRASSAMY
