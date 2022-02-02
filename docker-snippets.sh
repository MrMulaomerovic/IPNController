# create network
podman network create \
--subnet 10.10.11.0/29 \
payment_network

# run mysql container
podman run \
-d \
--name payment_mysql \
-p 3306:3306 \
-v '/usr/containers/payment_mysql:/var/lib/mysql' \
-e  MYSQL_ROOT_PASSWORD=root \
-e  MYSQL_DATABASE=payment \
-e  MYSQL_USER=user \
-e  MYSQL_PASSWORD=user \
--network payment_network \
mysql:8.0.28
