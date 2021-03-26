delete from user_role;
delete from usr;

insert into usr (id, active, password, username)
values (1, true, '$2a$08$7V8P5Cmhcb2iX6qKnAXLXu737yLCzO8.R21DYu.ipTtxFnFkfNHAW', 'admin'),
       (2, true, '$2a$08$I4c6UQUwDVhF0x/0NwHEkuccBafIapjCjfIckFgjgKHClvGyjkgHS', 'user');

insert into user_role (user_id, roles)
values (1, 'ADMIN'),
       (1, 'USER'),
       (1, 'USER');