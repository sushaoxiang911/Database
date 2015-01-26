/*initial database*/

\i setup_db.sql

/*
Exercise 1
a) 
*/
\d+ customers

/*
b)
*/
\d+
\di+

/*
c)
*/
SELECT count(*) FROM customers;

SELECT attname, n_distinct FROM pg_stats WHERE tablename = 'customers';

/*
d)
*/

SELECT COUNT (DISTINCT customerid) AS customerid,
       COUNT (DISTINCT firstname) AS firstname,
       COUNT (DISTINCT lastname) AS lastname, 
       COUNT (DISTINCT address1) AS address1,
       COUNT (DISTINCT address2) AS address2,
       COUNT (DISTINCT city) AS city,
       COUNT (DISTINCT state) AS state,
       COUNT (DISTINCT zip) AS zip,
       COUNT (DISTINCT country) AS country,
       COUNT (DISTINCT region) AS region,
       COUNT (DISTINCT email) AS email,
       COUNT (DISTINCT phone) AS phone,
       COUNT (DISTINCT creditcardtype) AS creditcardtype,
       COUNT (DISTINCT creditcard) AS creditcard,
       COUNT (DISTINCT creditcardexpiration) AS creditcardexpiration,
       COUNT (DISTINCT username) AS username,
       COUNT (DISTINCT password) AS password,
       COUNT (DISTINCT age) AS age,
       COUNT (DISTINCT income) AS income,
       COUNT (DISTINCT gender) AS gender
FROM customers;

/*
Exercise 2
*/

/*
a)
*/
EXPLAIN SELECT * FROM customers WHERE country = 'Japan';
SELECT COUNT(*) FROM customers WHERE country = 'Japan';


/*
b)
*/
SELECT relpages, reltuples FROM pg_class WHERE relname = 'customers';

/*
c)
*/



/*
Excercise 3
*/
\i setup_db.sql;
VACUUM;
ANALYZE;

CREATE INDEX customers_country ON customers(country);
VACUUM;
ANALYZE;

/*
a)
*/
SELECT relpages FROM pg_class WHERE relname = 'customers_country';

/*
b)
*/
EXPLAIN SELECT * FROM customers WHERE country = 'Japan';

/*
c)
*/

CLUSTER customers_country ON customers;
VACUUM;
ANALYZE;
/*
d)
*/
EXPLAIN SELECT * FROM customers WHERE country = 'Japan';

/*
Excercise 4
*/
\i setup_db.sql;
VACUUM;
ANALYZE;

/*
a)
*/
EXPLAIN SELECT totalamount 
	FROM Customers C, Orders O
	WHERE C.customerid = O.customerid AND C.country = 'Japan';


/*
b)
*/


/*
c)
*/
SET enable_hashjoin TO off;
EXPLAIN SELECT totalamount      
	FROM Customers C, Orders O 
	WHERE C.customerid = O.customerid AND C.country = 'Japan';

/*
d)
*/
SET enable_mergejoin TO off;
EXPLAIN SELECT totalamount      
	FROM Customers C, Orders O 
	WHERE C.customerid = O.customerid AND C.country = 'Japan';

/*
Excercise 5
*/

/*
a)
*/
\i setup_db.sql
VACUUM;
ANALYZE;

SET enable_hashjoin TO on;
SET enable_mergejoin TO on;

EXPLAIN SELECT AVG(totalamount) as avgOrder, country
	FROM Customers C, Orders O
	WHERE C.customerid = O.customerid
	GROUP BY country
	ORDER BY avgOrder;

SET enable_hashjoin TO off;

EXPLAIN SELECT AVG(totalamount) as avgOrder, country
	FROM Customers C, Orders O
	WHERE C.customerid = O.customerid
	GROUP BY country
	ORDER BY avgOrder;

/*
b)
*/
SET enable_hashjoin TO on;

EXPLAIN SELECT *
	FROM Customers C, Orders O
	WHERE C.customerid = O.customerid
	ORDER BY C.customerid;

SET enable_mergejoin TO off;

EXPLAIN SELECT *
	FROM Customers C, Orders O
	WHERE C.customerid = O.customerid
	ORDER BY C.customerid;



/*
Excercise 6
*/
/*
a)
*/
\i setup_db.sql
VACUUM;
ANALYZE;

EXPLAIN SELECT C.customerid, C.lastname
FROM Customers C
WHERE
4 < (
	SELECT COUNT(*)
	FROM Orders O
	WHERE O.customerid = C.customerid);

/*
b)
*/
CREATE VIEW OrderCount AS
	SELECT O.customerid, count(*) AS numorders
	FROM Orders O
	GROUP BY O.customerid;

/*
c)
*/
SELECT C.customerid, C.lastname
FROM Customers C, OrderCount O
WHERE C.customerid = O.customerid AND O.numorders > 4;

/*
d)
*/
EXPLAIN SELECT C.customerid, C.lastname
	FROM Customers C, OrderCount O
	WHERE C.customerid = O.customerid AND O.numorders > 4;

/*
Excercise 7
*/
/*
a)
*/
\i setup_db.sql
VACUUM;
ANALYZE;

EXPLAIN SELECT customerid, lastname, numorders
	FROM
	(
		SELECT C.customerid, C.lastname, count(*) as numorders
		FROM Customers C, Orders O
		WHERE C.customerid = O.customerid AND C.country = 'Japan'
		GROUP BY C.customerid, lastname
	) AS ORDERCOUNTS1
	WHERE 5 >= 
	(	
		SELECT count(*)
		FROM
		(
			SELECT C.customerid, C.lastname, count(*) as numorders
			FROM Customers C, Orders O
			WHERE C.customerid=O.customerid AND C.country = 'Japan'
			GROUP BY C.customerid, lastname
		) AS ORDERCOUNTS2
		WHERE ORDERCOUNTS1.numorders < ORDERCOUNTS2.numorders
	)
	ORDER BY customerid;

/*
b)
*/
CREATE VIEW OrderCountJapan AS
	SELECT O.customerid, count(*) AS numorders
	FROM Orders O, Customers C
	WHERE C.customerid = O.customerid AND C.country='Japan'
	GROUP BY O.customerid;

CREATE VIEW MoreFrequentJapanCustomers AS
	(	
		SELECT O3.customerid, 0 as ORank
		FROM OrderCountJapan O3
		WHERE O3.numorders IN (SELECT MAX(numorders) FROM OrderCountJapan)
	)	
	UNION
	(
		SELECT O2.customerid, count(*) as ORank
		FROM OrderCountJapan O2, OrderCountJapan O1
		WHERE O2.numorders < O1.numorders
		GROUP BY O2.customerid
	)
	ORDER BY 2;

/*
c)
*/
SELECT C.customerid, C.lastname, O.numorders
FROM Customers C, OrderCountJapan O, MoreFrequentJapanCustomers F
WHERE C.customerid = O.customerid AND C.customerid = F.customerid AND F.ORank <= 5
ORDER BY C.customerid;

/*
d)
*/
EXPLAIN SELECT C.customerid, C.lastname, O.numorders
FROM Customers C, OrderCountJapan O, MoreFrequentJapanCustomers F
WHERE C.customerid = O.customerid AND C.customerid = F.customerid AND F.ORank <= 5
ORDER BY C.customerid;










