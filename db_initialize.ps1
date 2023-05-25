#create db scheme, insert admin role
Get-Content ./server/bin/pizza_db.sql | C:/xampp/mysql/bin/mysql.exe --user=root --password=pizza4life --binary-mode
echo "Created DB Scheme"

#fill map data
$OutputEncoding = [Console]::InputEncoding = [Console]::OutputEncoding = [System.Text.UTF8Encoding]::new()
node ./client/src/business/FetchMapData.js > ./server/bin/map.sql
Get-Content ./server/bin/map.sql | C:/xampp/mysql/bin/mysql.exe --user=root --password=pizza4life --binary-mode
echo "Fetched API. Map data stored in DB"