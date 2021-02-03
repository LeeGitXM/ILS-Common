CREATE TABLE log(
	id bigint PRIMARY KEY auto_increment not null,
	process_id int NULL,
	thread bigint NULL,
	project char(25) NULL,
	scope char(10) NULL,
	client_id char(12) NULL,
	thread_name char(50) NULL,
	module varchar(250) NULL,
	logger_name varchar(100) NOT NULL,
	timestamp datetime NOT NULL,
	log_level int NULL,
	log_level_name char(10) NULL,
	log_message varchar(8000) NOT NULL,
	function_name varchar(100) NULL,
	line_number int NULL,
	retain_until datetime NOT NULL
);
