#post admin
$headerParams = @{"Authorization"="root"}
curl -Method GET -Uri http://localhost:8081/fill-data/roles -Header $headerParams
curl -Method GET -Uri http://localhost:8081/fill-data/users -Header $headerParams
curl -Method GET -Uri http://localhost:8081/fill-data/cars -Header $headerParams
curl -Method GET -Uri http://localhost:8081/fill-data/allergies -Header $headerParams
curl -Method GET -Uri http://localhost:8081/fill-data/ingredients -Header $headerParams
curl -Method GET -Uri http://localhost:8081/fill-data/menus -Header $headerParams
curl -Method GET -Uri http://localhost:8081/fill-data/menu-ingredients -Header $headerParams