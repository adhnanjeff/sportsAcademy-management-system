
-- Seed a default coach user (idempotent)
INSERT INTO users (
    email,
    password,
    first_name,
    last_name,
    full_name,
    date_of_birth,
    role,
    is_active,
    is_email_verified
)
SELECT
    'seed.coach@academy.com',
    'coach123',
    'Jawid',
    'Khan',
    'Jawid Khan',
    DATE '1995-01-18',
    'COACH',
    TRUE,
    TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM users u WHERE u.email = 'seed.coach@academy.com'
);

-- Seed coach profile row for the user above (idempotent)
INSERT INTO coaches (user_id, specialization, years_of_experience)
SELECT
    u.id,
    'General Training',
    5
FROM users u
WHERE u.email = 'seed.coach@academy.com'
  AND NOT EXISTS (
      SELECT 1 FROM coaches c WHERE c.user_id = u.id
  );

-- Ensure batch id=4 exists because the student mapping below targets batch_id=4
INSERT INTO batches (id, name, skill_level, coach_id, start_time, end_time, is_active)
SELECT
    4,
    'Beginner Batch',
    'BEGINNER',
    c.user_id,
    TIME '17:30:00',
    TIME '19:00:00',
    TRUE
FROM coaches c
JOIN users u ON u.id = c.user_id
WHERE u.email = 'seed.coach@academy.com'
  AND NOT EXISTS (
      SELECT 1 FROM batches b WHERE b.id = 4
  );

-- Keep sequence aligned after explicit id insert
SELECT setval(
    pg_get_serial_sequence('batches', 'id'),
    GREATEST((SELECT COALESCE(MAX(id), 1) FROM batches), 1),
    TRUE
);


INSERT INTO students 
(first_name, last_name, full_name, fee_payable, is_active, gender, monthly_fee_status, skill_level)
SELECT *
FROM (
VALUES
('Aadhav','Kannan','Aadhav Kannan',0,true,'MALE','UNPAID','BEGINNER'),
('Aadhya','Johri','Aadhya Johri',0,true,'FEMALE','UNPAID','BEGINNER'),
('Aaliya','Chawniwala','Aaliya Chawniwala',0,true,'FEMALE','UNPAID','BEGINNER'),
('Aarav','Chopra','Aarav Chopra',0,true,'MALE','UNPAID','BEGINNER'),
('Aarav','Gautam Gadwani','Aarav Gautam Gadwani',0,true,'MALE','UNPAID','BEGINNER'),
('Aarav','Valuparamvil Anulan','Aarav Valuparamvil Anulan',0,true,'MALE','UNPAID','BEGINNER'),
('Aarna','Hira','Aarna Hira',0,true,'FEMALE','UNPAID','BEGINNER'),
('Aaron','Abraham Philip','Aaron Abraham Philip',0,true,'MALE','UNPAID','BEGINNER'),
('Aaron','Chrish','Aaron Chrish',0,true,'MALE','UNPAID','BEGINNER'),
('Aaron','Jino','Aaron Jino',0,true,'MALE','UNPAID','BEGINNER'),
('Aaron','Rebello','Aaron Rebello',0,true,'MALE','UNPAID','BEGINNER'),
('Aashita','Upadhyay','Aashita Upadhyay',0,true,'FEMALE','UNPAID','BEGINNER'),
('Aashvi','Pandey','Aashvi Pandey',0,true,'FEMALE','UNPAID','BEGINNER'),
('Abbhigyan','Kishore','Abbhigyan Kishore',0,true,'MALE','UNPAID','BEGINNER'),
('Abdullah','Albaloushi','Abdullah Albaloushi',0,true,'MALE','UNPAID','BEGINNER'),
('Abdullah','Ejaz','Abdullah Ejaz',0,true,'MALE','UNPAID','BEGINNER'),
('Abhimanyu','Nair','Abhimanyu Nair',0,true,'MALE','UNPAID','BEGINNER'),
('Abner','Saldanha','Abner Saldanha',0,true,'MALE','UNPAID','BEGINNER'),
('Abrar','Rasul','Abrar Rasul',0,true,'MALE','UNPAID','BEGINNER'),
('Adam','','Adam',0,true,'MALE','UNPAID','BEGINNER'),
('Adelyn','Prasanth','Adelyn Prasanth',0,true,'FEMALE','UNPAID','BEGINNER'),
('Adhav','Ravikiran','Adhav Ravikiran',0,true,'MALE','UNPAID','BEGINNER'),
('Aditi','Valuparamvil Anulal','Aditi Valuparamvil Anulal',0,true,'FEMALE','UNPAID','BEGINNER'),
('Adler','Jude','Adler Jude',0,true,'MALE','UNPAID','BEGINNER'),
('Advika','Ram','Advika Ram',0,true,'FEMALE','UNPAID','BEGINNER'),
('Ahaan','Thomas Jinu','Ahaan Thomas Jinu',0,true,'MALE','UNPAID','BEGINNER'),
('Ahmad','Alduaij','Ahmad Alduaij',0,true,'MALE','UNPAID','BEGINNER'),
('Ahmed','Elsayed','Ahmed Elsayed',0,true,'MALE','UNPAID','BEGINNER'),
('Ahmed','Hidayathulla','Ahmed Hidayathulla',0,true,'MALE','UNPAID','BEGINNER'),
('Ahmed','Sameh','Ahmed Sameh',0,true,'MALE','UNPAID','BEGINNER'),
('Aiden','Anoop','Aiden Anoop',0,true,'MALE','UNPAID','BEGINNER'),
('Alayna','Fatima','Alayna Fatima',0,true,'FEMALE','UNPAID','BEGINNER'),
('Ali','Elsayed','Ali Elsayed',0,true,'MALE','UNPAID','BEGINNER'),
('Alika','Naik','Alika Naik',0,true,'FEMALE','UNPAID','BEGINNER'),
('Aliya','Azarudeen','Aliya Azarudeen',0,true,'FEMALE','UNPAID','BEGINNER'),
('Amal','Prince','Amal Prince',0,true,'MALE','UNPAID','BEGINNER'),
('Amanda','Anoop','Amanda Anoop',0,true,'FEMALE','UNPAID','BEGINNER'),
('Amatulla','Hakim Ji','Amatulla Hakim Ji',0,true,'FEMALE','UNPAID','BEGINNER'),
('Amaya','Azhar','Amaya Azhar',0,true,'FEMALE','UNPAID','BEGINNER'),
('Anaiyah','Susan','Anaiyah Susan',0,true,'FEMALE','UNPAID','BEGINNER'),
('Ananya','Xavier','Ananya Xavier',0,true,'FEMALE','UNPAID','BEGINNER'),
('Anaya','Nihalani','Anaya Nihalani',0,true,'FEMALE','UNPAID','BEGINNER'),
('Anika','Arun','Anika Arun',0,true,'FEMALE','UNPAID','BEGINNER'),
('Aniketh','Praveen','Aniketh Praveen',0,true,'MALE','UNPAID','BEGINNER'),
('Anna','Nugroho','Anna Nugroho',0,true,'FEMALE','UNPAID','BEGINNER'),
('Arjun','Arora','Arjun Arora',0,true,'MALE','UNPAID','BEGINNER'),
('Arya','Nagar','Arya Nagar',0,true,'FEMALE','UNPAID','BEGINNER'),
('Arya','Nair','Arya Nair',0,true,'FEMALE','UNPAID','BEGINNER'),
('Aryan','Kaimal Menacheril','Aryan Kaimal Menacheril',0,true,'MALE','UNPAID','BEGINNER'),
('Aryan','Motammari','Aryan Motammari',0,true,'MALE','UNPAID','BEGINNER'),
('Ashrith','Ram','Ashrith Ram',0,true,'MALE','UNPAID','BEGINNER'),
('Atiksh','Rao','Atiksh Rao',0,true,'MALE','UNPAID','BEGINNER'),
('Ayaan','Anoop','Ayaan Anoop',0,true,'MALE','UNPAID','BEGINNER')
) AS s(first_name,last_name,full_name,fee_payable,is_active,gender,monthly_fee_status,skill_level)
WHERE NOT EXISTS (
   SELECT 1 FROM students st WHERE st.full_name = s.full_name
);


INSERT INTO students 
(first_name, last_name, full_name, fee_payable, is_active, gender, monthly_fee_status, skill_level)
SELECT *
FROM (
VALUES
('Besma','Alduaij','Besma Alduaij',0,true,'FEMALE','UNPAID','BEGINNER'),
('Christa','Jacob','Christa Jacob',0,true,'FEMALE','UNPAID','BEGINNER'),
('Christa','Raichel','Christa Raichel',0,true,'FEMALE','UNPAID','BEGINNER'),
('Christna','Jino','Christna Jino',0,true,'FEMALE','UNPAID','BEGINNER'),
('Christy','Tom Shaiju','Christy Tom Shaiju',0,true,'FEMALE','UNPAID','BEGINNER'),
('Chrisvin','Shaiju','Chrisvin Shaiju',0,true,'MALE','UNPAID','BEGINNER'),
('Chrysal','Jacob','Chrysal Jacob',0,true,'FEMALE','UNPAID','BEGINNER'),
('Clarissa','Nicole','Clarissa Nicole',0,true,'FEMALE','UNPAID','BEGINNER'),
('Daim','Pirzada','Daim Pirzada',0,true,'MALE','UNPAID','BEGINNER'),
('Devanshi','Charday','Devanshi Charday',0,true,'FEMALE','UNPAID','BEGINNER'),
('Diya','Mercilyn','Diya Mercilyn',0,true,'FEMALE','UNPAID','BEGINNER'),
('Diya','Raoof','Diya Raoof',0,true,'FEMALE','UNPAID','BEGINNER'),
('Eisa','Mohamed Ismail','Eisa Mohamed Ismail',0,true,'MALE','UNPAID','BEGINNER'),
('Emily','Thomas','Emily Thomas',0,true,'FEMALE','UNPAID','BEGINNER'),
('Erin','Marshal','Erin Marshal',0,true,'FEMALE','UNPAID','BEGINNER'),
('Evan','Rony Mathew','Evan Rony Mathew',0,true,'MALE','UNPAID','BEGINNER'),
('Evaniya','','Evaniya',0,true,'FEMALE','UNPAID','BEGINNER'),
('Ezekiel','Eapen','Ezekiel Eapen',0,true,'MALE','UNPAID','BEGINNER'),
('Farhan','Sheik','Farhan Sheik',0,true,'MALE','UNPAID','BEGINNER'),
('Farida','','Farida',0,true,'FEMALE','UNPAID','BEGINNER'),
('Fatma','Ahmad','Fatma Ahmad',0,true,'FEMALE','UNPAID','BEGINNER'),
('Gautam','Menon','Gautam Menon',0,true,'MALE','UNPAID','BEGINNER'),
('Haajar','Azarudeen','Haajar Azarudeen',0,true,'FEMALE','UNPAID','BEGINNER'),
('Hadi','Yuan','Hadi Yuan',0,true,'MALE','UNPAID','BEGINNER'),
('Hala','Hassan Abul','Hala Hassan Abul',0,true,'FEMALE','UNPAID','BEGINNER'),
('Hamdan','Ahmed','Hamdan Ahmed',0,true,'MALE','UNPAID','BEGINNER'),
('Hana','Shafraz','Hana Shafraz',0,true,'FEMALE','UNPAID','BEGINNER'),
('Harnoor','Kaur','Harnoor Kaur',0,true,'FEMALE','UNPAID','BEGINNER'),
('Hasan','Virpurwala','Hasan Virpurwala',0,true,'MALE','UNPAID','BEGINNER'),
('Hasnain','Mazahir','Hasnain Mazahir',0,true,'MALE','UNPAID','BEGINNER'),
('Hisham','Shafraz','Hisham Shafraz',0,true,'MALE','UNPAID','BEGINNER'),
('Huda','Abdul Rahman','Huda Abdul Rahman',0,true,'FEMALE','UNPAID','BEGINNER'),
('Hussain','M','Hussain M',0,true,'MALE','UNPAID','BEGINNER'),
('Huzair','','Huzair',0,true,'MALE','UNPAID','BEGINNER'),
('Ian','John','Ian John',0,true,'MALE','UNPAID','BEGINNER'),
('Ibrahim','','Ibrahim',0,true,'MALE','UNPAID','BEGINNER'),
('Isha','Bhavani','Isha Bhavani',0,true,'FEMALE','UNPAID','BEGINNER'),
('Ishaanvi','Srihari','Ishaanvi Srihari',0,true,'FEMALE','UNPAID','BEGINNER'),
('Ivan','Varghese','Ivan Varghese',0,true,'MALE','UNPAID','BEGINNER'),
('Jayden','Jose Mathew','Jayden Jose Mathew',0,true,'MALE','UNPAID','BEGINNER'),
('Jeevansh','Juneja','Jeevansh Juneja',0,true,'MALE','UNPAID','BEGINNER'),
('Jewel','Juby','Jewel Juby',0,true,'FEMALE','UNPAID','BEGINNER'),
('Joan','Rani','Joan Rani',0,true,'FEMALE','UNPAID','BEGINNER'),
('Joanna','Harsan','Joanna Harsan',0,true,'FEMALE','UNPAID','BEGINNER'),
('Joanna','Jeswin','Joanna Jeswin',0,true,'FEMALE','UNPAID','BEGINNER'),
('Joash','John Vembillil','Joash John Vembillil',0,true,'MALE','UNPAID','BEGINNER'),
('Johan','Juby','Johan Juby',0,true,'MALE','UNPAID','BEGINNER'),
('Johann','Saju','Johann Saju',0,true,'MALE','UNPAID','BEGINNER'),
('Johann','Saju Varghese','Johann Saju Varghese',0,true,'MALE','UNPAID','BEGINNER'),
('Johanna','Alphonsa Jyothis','Johanna Alphonsa Jyothis',0,true,'FEMALE','UNPAID','BEGINNER'),
('Johanna','Judah','Johanna Judah',0,true,'FEMALE','UNPAID','BEGINNER'),
('John','Mathew','John Mathew',0,true,'MALE','UNPAID','BEGINNER'),
('Josh','Johnitty','Josh Johnitty',0,true,'MALE','UNPAID','BEGINNER'),
('Kabir','Nagi','Kabir Nagi',0,true,'MALE','UNPAID','BEGINNER'),
('Karim','Mohamed','Karim Mohamed',0,true,'MALE','UNPAID','BEGINNER'),
('Kenneth','Reji George','Kenneth Reji George',0,true,'MALE','UNPAID','BEGINNER'),
('Kevin','Emmanual','Kevin Emmanual',0,true,'MALE','UNPAID','BEGINNER'),
('Khadija','Sameerudeen','Khadija Sameerudeen',0,true,'FEMALE','UNPAID','BEGINNER'),
('Kris','Ann Shelly','Kris Ann Shelly',0,true,'MALE','UNPAID','BEGINNER'),
('Kyra','Mariam John','Kyra Mariam John',0,true,'FEMALE','UNPAID','BEGINNER'),
('Laksshint','Vinod','Laksshint Vinod',0,true,'MALE','UNPAID','BEGINNER'),
('Leen','Farha Fathima','Leen Farha Fathima',0,true,'FEMALE','UNPAID','BEGINNER'),
('Liesel','Mendonca','Liesel Mendonca',0,true,'FEMALE','UNPAID','BEGINNER'),
('Lyra','Rosalne','Lyra Rosalne',0,true,'FEMALE','UNPAID','BEGINNER'),
('Mahnav','Saai','Mahnav Saai',0,true,'MALE','UNPAID','BEGINNER'),
('Manasa','Chithy','Manasa Chithy',0,true,'FEMALE','UNPAID','BEGINNER'),
('Meeval','Rony Mathew','Meeval Rony Mathew',0,true,'FEMALE','UNPAID','BEGINNER'),
('Mia','Lee','Mia Lee',0,true,'FEMALE','UNPAID','BEGINNER'),
('Mithran','Gnana','Mithran Gnana',0,true,'MALE','UNPAID','BEGINNER'),
('Mohamad','Zabiullah Khan','Mohamad Zabiullah Khan',0,true,'MALE','UNPAID','BEGINNER'),
('Mohamed','Nehan','Mohamed Nehan',0,true,'MALE','UNPAID','BEGINNER'),
('Mohamed','Rafin','Mohamed Rafin',0,true,'MALE','UNPAID','BEGINNER'),
('Mohammad','Aiman','Mohammad Aiman',0,true,'MALE','UNPAID','BEGINNER'),
('Mohammed','Ayaan','Mohammed Ayaan',0,true,'MALE','UNPAID','BEGINNER'),
('Mohammed','Naif','Mohammed Naif',0,true,'MALE','UNPAID','BEGINNER'),
('Mohammed','Nasheeth','Mohammed Nasheeth',0,true,'MALE','UNPAID','BEGINNER'),
('Mohammed','Yosuf Zaeen','Mohammed Yosuf Zaeen',0,true,'MALE','UNPAID','BEGINNER'),
('Muhammed','Muaadh','Muhammed Muaadh',0,true,'MALE','UNPAID','BEGINNER'),
('Myra','Singh','Myra Singh',0,true,'FEMALE','UNPAID','BEGINNER'),
('Mythay','Rampor','Mythay Rampor',0,true,'MALE','UNPAID','BEGINNER'),
('Nabeeha','Mubin','Nabeeha Mubin',0,true,'FEMALE','UNPAID','BEGINNER'),
('Naina','Jacob','Naina Jacob',0,true,'FEMALE','UNPAID','BEGINNER'),
('Naisa','Mariam','Naisa Mariam',0,true,'FEMALE','UNPAID','BEGINNER'),
('Nandita','Varghese','Nandita Varghese',0,true,'FEMALE','UNPAID','BEGINNER'),
('Nataania','Maria','Nataania Maria',0,true,'FEMALE','UNPAID','BEGINNER'),
('Nathan','Amit','Nathan Amit',0,true,'MALE','UNPAID','BEGINNER'),
('Nathan','Britto','Nathan Britto',0,true,'MALE','UNPAID','BEGINNER'),
('Nathan','Jacob','Nathan Jacob',0,true,'MALE','UNPAID','BEGINNER'),
('Nathan','Joe','Nathan Joe',0,true,'MALE','UNPAID','BEGINNER'),
('Nathan','Kaimal','Nathan Kaimal',0,true,'MALE','UNPAID','BEGINNER'),
('Nathaniel','Aby','Nathaniel Aby',0,true,'MALE','UNPAID','BEGINNER'),
('Navya','Nair','Navya Nair',0,true,'FEMALE','UNPAID','BEGINNER'),
('Nicolt','Susan','Nicolt Susan',0,true,'FEMALE','UNPAID','BEGINNER'),
('Niddin','','Niddin',0,true,'MALE','UNPAID','BEGINNER'),
('Nuha','Raoof','Nuha Raoof',0,true,'FEMALE','UNPAID','BEGINNER'),
('Nynieshia','Elizabeth','Nynieshia Elizabeth',0,true,'FEMALE','UNPAID','BEGINNER'),
('Prayan','Wadhwani','Prayan Wadhwani',0,true,'MALE','UNPAID','BEGINNER'),
('Qusai','Kapadia','Qusai Kapadia',0,true,'MALE','UNPAID','BEGINNER'),
('Rachel','Mary Ephrem','Rachel Mary Ephrem',0,true,'FEMALE','UNPAID','BEGINNER'),
('Raheal','Lysa Sherry','Raheal Lysa Sherry',0,true,'FEMALE','UNPAID','BEGINNER'),
('Rayan','Khan','Rayan Khan',0,true,'MALE','UNPAID','BEGINNER'),
('Reanne','Pinto','Reanne Pinto',0,true,'FEMALE','UNPAID','BEGINNER'),
('Rehan','Hameed','Rehan Hameed',0,true,'MALE','UNPAID','BEGINNER'),
('Reiner','Mendonca','Reiner Mendonca',0,true,'MALE','UNPAID','BEGINNER'),
('Reuben','Reji Varghese','Reuben Reji Varghese',0,true,'MALE','UNPAID','BEGINNER'),
('Reyam','','Reyam',0,true,'FEMALE','UNPAID','BEGINNER'),
('Reyansh','Sinha','Reyansh Sinha',0,true,'MALE','UNPAID','BEGINNER'),
('Riana','Abdelrahman Ahmed','Riana Abdelrahman Ahmed',0,true,'FEMALE','UNPAID','BEGINNER'),
('Rishabh','Raj','Rishabh Raj',0,true,'MALE','UNPAID','BEGINNER'),
('Riyola','Mendonca','Riyola Mendonca',0,true,'FEMALE','UNPAID','BEGINNER'),
('Rylynn','Gonsalves','Rylynn Gonsalves',0,true,'FEMALE','UNPAID','BEGINNER'),
('Saanvi','Bhavani','Saanvi Bhavani',0,true,'FEMALE','UNPAID','BEGINNER'),
('Saara','Hasana','Saara Hasana',0,true,'FEMALE','UNPAID','BEGINNER'),
('Sai','Akshaj','Sai Akshaj',0,true,'MALE','UNPAID','BEGINNER'),
('Sai','Avyukth','Sai Avyukth',0,true,'MALE','UNPAID','BEGINNER'),
('Sai','Ishan Adivi','Sai Ishan Adivi',0,true,'MALE','UNPAID','BEGINNER'),
('Saidarshan','','Saidarshan',0,true,'MALE','UNPAID','BEGINNER'),
('Saidhikshaa','','Saidhikshaa',0,true,'FEMALE','UNPAID','BEGINNER'),
('Saira','Aby','Saira Aby',0,true,'FEMALE','UNPAID','BEGINNER'),
('Samshruth','Bose','Samshruth Bose',0,true,'MALE','UNPAID','BEGINNER'),
('Sarah','Chinnu Sam','Sarah Chinnu Sam',0,true,'FEMALE','UNPAID','BEGINNER'),
('Sarah','Sameeha','Sarah Sameeha',0,true,'FEMALE','UNPAID','BEGINNER'),
('Sasha','','Sasha',0,true,'FEMALE','UNPAID','BEGINNER'),
('Shreyas','','Shreyas',0,true,'MALE','UNPAID','BEGINNER'),
('Sofiia','Ezzi','Sofiia Ezzi',0,true,'FEMALE','UNPAID','BEGINNER'),
('Sreesai','Hanvi','Sreesai Hanvi',0,true,'MALE','UNPAID','BEGINNER'),
('Sreya','Kadiyala','Sreya Kadiyala',0,true,'FEMALE','UNPAID','BEGINNER'),
('Stephanie','Harik','Stephanie Harik',0,true,'FEMALE','UNPAID','BEGINNER'),
('Steve','Mohan Mathew','Steve Mohan Mathew',0,true,'MALE','UNPAID','BEGINNER'),
('Steven','Thomas','Steven Thomas',0,true,'MALE','UNPAID','BEGINNER'),
('Syed','Hasnain','Syed Hasnain',0,true,'MALE','UNPAID','BEGINNER'),
('Syed','Salaah','Syed Salaah',0,true,'MALE','UNPAID','BEGINNER'),
('Tania','Faith','Tania Faith',0,true,'FEMALE','UNPAID','BEGINNER'),
('Tanika','Naik','Tanika Naik',0,true,'FEMALE','UNPAID','BEGINNER'),
('Tanvi','Chowdesha','Tanvi Chowdesha',0,true,'FEMALE','UNPAID','BEGINNER'),
('Tanvisha','Esther','Tanvisha Esther',0,true,'FEMALE','UNPAID','BEGINNER'),
('Tanya','Chowdesha','Tanya Chowdesha',0,true,'FEMALE','UNPAID','BEGINNER'),
('Taseen','Mohamed','Taseen Mohamed',0,true,'MALE','UNPAID','BEGINNER'),
('Thomas','Josey','Thomas Josey',0,true,'MALE','UNPAID','BEGINNER'),
('Umeir','Milhan','Umeir Milhan',0,true,'MALE','UNPAID','BEGINNER'),
('Valentian','','Valentian',0,true,'MALE','UNPAID','BEGINNER'),
('Varasala','Roshan Kiran','Varasala Roshan Kiran',0,true,'MALE','UNPAID','BEGINNER'),
('Vedhika','Vijikumar','Vedhika Vijikumar',0,true,'FEMALE','UNPAID','BEGINNER'),
('Veronica','Liz Sherry','Veronica Liz Sherry',0,true,'FEMALE','UNPAID','BEGINNER'),
('Vidyaa','Gayatri','Vidyaa Gayatri',0,true,'FEMALE','UNPAID','BEGINNER'),
('Vinitra','Vinod','Vinitra Vinod',0,true,'FEMALE','UNPAID','BEGINNER'),
('Yahya','Azarudeen','Yahya Azarudeen',0,true,'MALE','UNPAID','BEGINNER'),
('Yerin','Yoon','Yerin Yoon',0,true,'FEMALE','UNPAID','BEGINNER'),
('Yohan','Joseph','Yohan Joseph',0,true,'MALE','UNPAID','BEGINNER'),
('Yuan','Hadi','Yuan Hadi',0,true,'MALE','UNPAID','BEGINNER'),
('Yugan','','Yugan',0,true,'MALE','UNPAID','BEGINNER'),
('Yugan','Vijayaraj','Yugan Vijayaraj',0,true,'MALE','UNPAID','BEGINNER'),
('Yunu','Kim','Yunu Kim',0,true,'MALE','UNPAID','BEGINNER'),
('Yusra','Syed','Yusra Syed',0,true,'FEMALE','UNPAID','BEGINNER'),
('Zahra','','Zahra',0,true,'FEMALE','UNPAID','BEGINNER'),
('Zainab','Mohammed','Zainab Mohammed',0,true,'FEMALE','UNPAID','BEGINNER'),
('Zohaib','Hasnain','Zohaib Hasnain',0,true,'MALE','UNPAID','BEGINNER')
) AS s(first_name,last_name,full_name,fee_payable,is_active,gender,monthly_fee_status,skill_level)
WHERE NOT EXISTS (
   SELECT 1 FROM students st WHERE st.full_name = s.full_name
);


INSERT INTO batch_students (batch_id, student_id)
SELECT 4, s.id
FROM students s
WHERE EXISTS (SELECT 1 FROM batches b WHERE b.id = 4)
AND NOT EXISTS (
    SELECT 1
    FROM batch_students bs
    WHERE bs.batch_id = 4
    AND bs.student_id = s.id
);
