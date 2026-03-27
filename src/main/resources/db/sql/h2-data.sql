-- ADMIN 계정 (password: admin123)
insert into members (username, password, name, role, created_date)
values ('admin', '$2b$10$29rgRsGobkYa5OMISJ.MguLCvT1NiIUzvvJIoomA7K5FvEQKSOdCu', '관리자', 'ADMIN', now());

-- USER 계정 (password: user123)
insert into members (username, password, name, role, created_date)
values ('user1', '$2b$10$OxgSXhIzjLQFeRELT7ClB.odws7Ul0goxJdE/80n9BrTjbkwm.89W', '사용자1', 'USER', now());

-- 샘플 콘텐츠
insert into contents (title, description, view_count, created_date, created_by)
values ('첫 번째 콘텐츠', '첫 번째 콘텐츠 내용입니다.', 0, now(), 'admin');

insert into contents (title, description, view_count, created_date, created_by)
values ('두 번째 콘텐츠', '두 번째 콘텐츠 내용입니다.', 0, now(), 'user1');
