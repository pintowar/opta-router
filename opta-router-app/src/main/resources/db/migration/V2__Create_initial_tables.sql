CREATE SEQUENCE vrp_problem_pk_seq
	INCREMENT BY 1
	MINVALUE 0
	MAXVALUE 2147483647
	START WITH 1
	CACHE 1
	NO CYCLE;

CREATE TABLE vrp_problem (
    id BIGINT DEFAULT NEXT VALUE FOR vrp_problem_pk_seq PRIMARY KEY,
    name VARCHAR (100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE vrp_problem_matrix (
    vrp_problem_id BIGINT PRIMARY KEY,
    location_ids BIGINT ARRAY NOT NULL,
    travel_distances DOUBLE PRECISION ARRAY NOT NULL,
    travel_times BIGINT ARRAY NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (vrp_problem_id) REFERENCES vrp_problem(id)
);

CREATE SEQUENCE vrp_location_pk_seq
	INCREMENT BY 1
	MINVALUE 0
	MAXVALUE 2147483647
	START WITH 1
	CACHE 1
	NO CYCLE;

CREATE TABLE vrp_location (
    id BIGINT DEFAULT NEXT VALUE FOR location_pk_seq PRIMARY KEY,
    name VARCHAR (100) NOT NULL,
    latitude DECIMAL(9, 6) NOT NULL,
    longitude DECIMAL(9, 6) NOT NULL,
    kind VARCHAR (20) NOT NULL,
    demand INT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CHECK (kind in ('depot', 'customer'))
);

CREATE TABLE vrp_problem_location (
    vrp_problem_id BIGINT,
    location_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (vrp_problem_id) REFERENCES vrp_problem(id),
    FOREIGN KEY (location_id) REFERENCES vrp_location(id)
);

CREATE UNIQUE INDEX idx_unique_vrp_problem_location ON vrp_problem_location (
    vrp_problem_id, location_id
);

CREATE SEQUENCE vrp_vehicle_pk_seq
	INCREMENT BY 1
	MINVALUE 0
	MAXVALUE 2147483647
	START WITH 1
	CACHE 1
	NO CYCLE;

CREATE TABLE vrp_vehicle (
    id BIGINT DEFAULT NEXT VALUE FOR vrp_vehicle_pk_seq PRIMARY KEY,
    name VARCHAR (100) NOT NULL,
    capacity INT NOT NULL,
    depot_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (depot_id) REFERENCES vrp_location(id)
);

CREATE SEQUENCE vrp_solver_solution_pk_seq
	INCREMENT BY 1
	MINVALUE 0
	MAXVALUE 2147483647
	START WITH 1
	CACHE 1
	NO CYCLE;

CREATE TABLE vrp_solver_solution (
    id BIGINT DEFAULT NEXT VALUE FOR vrp_solver_solution_pk_seq PRIMARY KEY,
    vrp_problem_id BIGINT NOT NULL,
    solver VARCHAR (50),
    solution_key UUID,
    status VARCHAR (20) NOT NULL,
    paths JSON NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (vrp_problem_id) REFERENCES vrp_problem(id)
);