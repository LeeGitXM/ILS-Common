CREATE TABLE log(
	id bigint PRIMARY KEY auto_increment not null,
	process_id int NULL,
	thread bigint NULL,
	project char(25) NULL,
	scope char(10) NULL,
	client_id char(12) NULL,
	thread_name char(50) NULL,
	module char(50) NULL,
	logger_name char(50) NOT NULL,
	timestamp datetime NOT NULL,
	log_level int NULL,
	log_levelname char(10) NULL,
	log_message varchar(4000) NOT NULL,
	function_name char(25) NULL,
	filename char(25) NULL,
	line_number int NULL,
	retain_until datetime NOT NULL
);
