CREATE SEQUENCE location_pk_seq
	INCREMENT BY 1
	MINVALUE 0
	MAXVALUE 2147483647
	START WITH 1
	CACHE 1
	NO CYCLE;

CREATE TABLE location (
    id BIGINT DEFAULT NEXT VALUE FOR location_pk_seq PRIMARY KEY,
    name VARCHAR (100) NOT NULL,
    latitude DECIMAL(9, 6) NOT NULL,
    longitude DECIMAL(9, 6) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE SEQUENCE depot_pk_seq
	INCREMENT BY 1
	MINVALUE 0
	MAXVALUE 2147483647
	START WITH 1
	CACHE 1
	NO CYCLE;

CREATE TABLE depot (
    id BIGINT DEFAULT NEXT VALUE FOR depot_pk_seq PRIMARY KEY,
    name VARCHAR (100) NOT NULL,
    location_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (location_id) REFERENCES location(id)
);

CREATE SEQUENCE vehicle_pk_seq
	INCREMENT BY 1
	MINVALUE 0
	MAXVALUE 2147483647
	START WITH 1
	CACHE 1
	NO CYCLE;

CREATE TABLE vehicle (
    id BIGINT DEFAULT NEXT VALUE FOR vehicle_pk_seq PRIMARY KEY,
    name VARCHAR (100) NOT NULL,
    capacity INT NOT NULL,
    depot_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (depot_id) REFERENCES depot(id)
);

CREATE SEQUENCE customer_pk_seq
	INCREMENT BY 1
	MINVALUE 0
	MAXVALUE 2147483647
	START WITH 1
	CACHE 1
	NO CYCLE;

CREATE TABLE customer (
    id BIGINT DEFAULT NEXT VALUE FOR customer_pk_seq PRIMARY KEY,
    name VARCHAR (100) NOT NULL,
    demand INT NOT NULL,
    location_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (location_id) REFERENCES location(id)
);

CREATE SEQUENCE route_pk_seq
	INCREMENT BY 1
	MINVALUE 0
	MAXVALUE 2147483647
	START WITH 1
	CACHE 1
	NO CYCLE;

CREATE TABLE route (
    id BIGINT DEFAULT NEXT VALUE FOR route_pk_seq PRIMARY KEY,
    name VARCHAR (100) NOT NULL,
    depot_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (depot_id) REFERENCES depot(id)
);

CREATE TABLE route_customer (
    route_id BIGINT,
    customer_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (route_id) REFERENCES route(id),
    FOREIGN KEY (customer_id) REFERENCES customer(id)
);

CREATE UNIQUE INDEX idx_unique_route_customer ON route_customer (
    route_id, customer_id
);

CREATE TABLE route_matrix (
    route_id BIGINT PRIMARY KEY,
    matrix JSON NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (route_id) REFERENCES route(id)
);

CREATE SEQUENCE solution_pk_seq
	INCREMENT BY 1
	MINVALUE 0
	MAXVALUE 2147483647
	START WITH 1
	CACHE 1
	NO CYCLE;

CREATE TABLE solution (
    id BIGINT DEFAULT NEXT VALUE FOR solution_pk_seq PRIMARY KEY,
    solver_key UUID,
    route_id BIGINT NOT NULL,
    status VARCHAR (20) NOT NULL,
    paths JSON NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (route_id) REFERENCES route(id)
);