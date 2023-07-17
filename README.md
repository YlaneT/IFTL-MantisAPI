# Mantis API

API Java Spring simulant une API Mantisbt à l'aide de Selenium

## Endpoints

| endpoint             | page (de mantisbt) | valeurs de retour                                                                         |
|----------------------|--------------------|-------------------------------------------------------------------------------------------|
| My view              | my view            | unassigned, resolved,<br> reported by me, monitored by me and<br>recently modified issues |
| My view detail       | my view            |                                                                                           |
| Search issue         | view issues        | List d'issues                                                                             |
| Change Log           | change log         | ?                                                                                         |
| Road map             | Road map           | ?                                                                                         |
| Summary  by status   | summary            | Nombre d'issue par status                                                                 |
| Summary  by severity | summary            | Nombre d'issue par sévérité                                                               |
| Summary  by category | summary            | Nombre d'issue par catégorie                                                              |
| Time stats           | summary            | Statistiques<br/>- longuest open<br/>- average time<br/>- total time                      |
| By date              | summary            | Nombre d'issue ouverte, résolue et %age dans la timeframe                                 |

:warning: La liste des endpoints n'est pas exhaustive

## A ajouter

Summary, My account ?, jump to issue
