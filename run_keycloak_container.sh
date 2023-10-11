docker stop keycloak-22_0
docker rm keycloak-22_0
docker run --name keycloak-22_0 -p 8000:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin -v ~/Documents/Services/Keycloak_22.0.4/data:/opt/keycloak/data quay.io/keycloak/keycloak:22.0.4 start-dev