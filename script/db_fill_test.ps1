#post admin
$headerParams = @{"Authorization"="root"}
curl -Method GET -Uri http://localhost:8081/fill-data/roles -Header $headerParams -UseBasicParsing
curl -Method GET -Uri http://localhost:8081/fill-data/users -Header $headerParams -UseBasicParsing
curl -Method GET -Uri http://localhost:8081/fill-data/cars -Header $headerParams -UseBasicParsing
curl -Method GET -Uri http://localhost:8081/fill-data/allergies -Header $headerParams -UseBasicParsing
curl -Method GET -Uri http://localhost:8081/fill-data/ingredients -Header $headerParams -UseBasicParsing
curl -Method GET -Uri http://localhost:8081/fill-data/menus -Header $headerParams -UseBasicParsing
curl -Method GET -Uri http://localhost:8081/fill-data/menu-ingredients -Header $headerParams -UseBasicParsing