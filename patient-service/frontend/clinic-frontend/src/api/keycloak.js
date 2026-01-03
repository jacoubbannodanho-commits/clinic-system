import Keycloak from "keycloak-js";

const keycloak = new Keycloak({
    url: "http://localhost:30080",
    realm: "clinic",
    clientId: "clinic-spa"
});

export default keycloak;
