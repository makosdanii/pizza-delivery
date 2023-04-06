CREATE DATABASE if not exists pizza_delivery;
USE pizza_delivery;

drop table if exists food_order;
drop table if exists inventory;
drop table if exists car_ingredient;

drop table if exists car;
drop table if exists user;
drop table if exists role;

drop table if exists map;
drop table if exists edge;
drop table if exists street_name;

drop table if exists menu_ingredient;
drop table if exists menu;
drop table if exists ingredient;
drop table if exists allergy;

-- cooking
create table allergy (
    id int not null auto_increment,
    name varchar(64) not null unique,
    PRIMARY KEY (id)
);

create table ingredient (
    id int not null auto_increment,
    name varchar(64) not null unique,
    allergy_id int,
    PRIMARY KEY (id),
    FOREIGN KEY (allergy_id) REFERENCES allergy(id)
);

create table menu (
    id int not null auto_increment,
    name varchar(64) not null unique,
    price tinyint not null,
    PRIMARY KEY (id)
);

create table menu_ingredient(
    menu_id int not null,
    ingredient_id int not null,
    quantity tinyint not null,
    FOREIGN KEY (menu_id) REFERENCES menu(id),
    FOREIGN KEY (ingredient_id) REFERENCES ingredient(id)
);
-- navigating
create table street_name(
    id int not null auto_increment,
    that varchar(64) not null unique,
    until_no tinyint not null,
    PRIMARY KEY (id)
);

create table edge(
    id int not null auto_increment,
    vertex tinyint not null, -- range of house no. on the edge
    edge_name int not null,
    edge_weight int not null,
    PRIMARY KEY (id),
    FOREIGN KEY (edge_name) REFERENCES street_name(id)
);

create table map(
    edge_id int not null,
    neighbour_id int not null,
    FOREIGN KEY (edge_id) REFERENCES edge(id),
    FOREIGN KEY (neighbour_id) REFERENCES edge(id)
);

-- workers
create table role(
    id int not null auto_increment,
    name varchar(64) not null unique,
    PRIMARY KEY (id)
);

create table user(
    id int not null auto_increment,
    email varchar(64) not null unique,
    name varchar(64) not null,
    password varchar(128) not null,
    street_name_id int not null,
    role_id int not null,
    PRIMARY KEY (id),
    FOREIGN KEY (street_name_id) REFERENCES street_name(id),
    FOREIGN KEY (role_id) REFERENCES role(id)
);

create table car (
    id int not null auto_increment,
    license varchar(16) unique,
    user_id int not null,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES user(id)
);

-- operating
create table car_ingredient(
    car_id int not null,
    ingredient_id int not null,
    current_percent tinyint not null,
    modified_at timestamp not null,
    FOREIGN KEY (car_id) REFERENCES car(id),
    FOREIGN KEY (ingredient_id) REFERENCES ingredient(id)
);

create table inventory (
    expense tinyint, -- null if taken for reselling
    car_id int not null,
    ingredient_id int not null,
    current_qt int not null,
    modified_at timestamp not null,
    FOREIGN KEY (car_id) REFERENCES car(id),
    FOREIGN KEY (ingredient_id) REFERENCES ingredient(id)
);

create table food_order(
    user_id int not null,
    car_id int not null,
    menu_id int not null,
    ordered_at timestamp not null,
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (car_id) REFERENCES car(id),
    FOREIGN KEY (menu_id) REFERENCES menu(id)
);

insert into role(name) value ('admin');
insert into user(email, name, password, role_id) value ('admin@pizzadomain.com', 'secret', 1);
