#post admin
$headerParams = @{"Authorization"="root"}
curl -Method GET -Uri http://localhost:8081/fill-data/basic -Header $headerParams -UseBasicParsing