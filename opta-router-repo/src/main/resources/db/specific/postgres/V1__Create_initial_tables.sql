CREATE SEQUENCE vrp_problem_pk_seq
	INCREMENT BY 1
	MINVALUE 0
	MAXVALUE 2147483647
	START WITH 1
	CACHE 1
	NO CYCLE;

CREATE TABLE vrp_problem (
    id BIGINT NOT NULL DEFAULT nextval('vrp_problem_pk_seq'),
    name VARCHAR (100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY(id)
);

CREATE TABLE vrp_problem_matrix (
    vrp_problem_id BIGINT PRIMARY KEY,
    location_ids BIGINT ARRAY NOT NULL,
    travel_distances DOUBLE PRECISION ARRAY NOT NULL,
    travel_times BIGINT ARRAY NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_vrp_problem FOREIGN KEY(vrp_problem_id) REFERENCES vrp_problem(id) ON DELETE CASCADE
);

CREATE SEQUENCE location_pk_seq
	INCREMENT BY 1
	MINVALUE 0
	MAXVALUE 2147483647
	START WITH 1
	CACHE 1
	NO CYCLE;

CREATE TABLE location (
    id BIGINT NOT NULL DEFAULT nextval('location_pk_seq'),
    name VARCHAR (100) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    kind VARCHAR (20) NOT NULL,
    demand INT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY(id),
    CHECK (kind in ('depot', 'customer'))
);

CREATE TABLE vrp_problem_location (
    vrp_problem_id BIGINT,
    location_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_vrp_problem FOREIGN KEY(vrp_problem_id) REFERENCES vrp_problem(id) ON DELETE CASCADE,
    CONSTRAINT fk_location FOREIGN KEY(location_id) REFERENCES location(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX idx_unique_vrp_problem_location ON vrp_problem_location (
    vrp_problem_id, location_id
);

CREATE SEQUENCE vehicle_pk_seq
	INCREMENT BY 1
	MINVALUE 0
	MAXVALUE 2147483647
	START WITH 1
	CACHE 1
	NO CYCLE;

CREATE TABLE vehicle (
    id BIGINT NOT NULL DEFAULT nextval('vehicle_pk_seq'),
    name VARCHAR (100) NOT NULL,
    capacity INT NOT NULL,
    depot_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY(id),
    CONSTRAINT fk_depot FOREIGN KEY(depot_id) REFERENCES location(id)
);

CREATE TABLE vrp_solver_request (
    request_key UUID NOT NULL,
    vrp_problem_id BIGINT NOT NULL,
    solver VARCHAR (50) NOT NULL,
    status VARCHAR (20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY(request_key),
    CONSTRAINT fk_vrp_problem FOREIGN KEY(vrp_problem_id) REFERENCES vrp_problem(id)
);

CREATE SEQUENCE vrp_solver_solution_pk_seq
	INCREMENT BY 1
	MINVALUE 0
	MAXVALUE 2147483647
	START WITH 1
	CACHE 1
	NO CYCLE;

CREATE TABLE vrp_solver_solution (
    id BIGINT NOT NULL DEFAULT nextval('vrp_solver_solution_pk_seq'),
    vrp_problem_id BIGINT NOT NULL,
    request_key UUID,
    status VARCHAR (20) NOT NULL,
    objective DOUBLE PRECISION NOT NULL,
    paths JSON NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY(id),
    CONSTRAINT fk_vrp_problem FOREIGN KEY(vrp_problem_id) REFERENCES vrp_problem(id) ON DELETE CASCADE,
    CONSTRAINT fk_vrp_solver_request FOREIGN KEY(request_key) REFERENCES vrp_solver_request(request_key)
);

CREATE TABLE vrp_solution (
    vrp_problem_id BIGINT NOT NULL,
    paths JSON NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY(vrp_problem_id),
    CONSTRAINT fk_vrp_problem FOREIGN KEY(vrp_problem_id) REFERENCES vrp_problem(id) ON DELETE CASCADE
);