delete from message;

insert into message (id, text, tag, user_id)
values (1, 'first', 'same-text', 1),
       (2, 'second', 'dif_text', 1),
       (3, 'third', 'same-text', 1),
       (4, 'fifth', 'something-text', 1);

alter sequence hibernate_sequence restart with 10;