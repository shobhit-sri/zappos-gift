zappos-gift
===========

Challenge 1 solution

1. The solution is built in Java and used javax.json-1.0.4.jar for parsing JSON message extracted by calling Zappos API.
2. The program asks user to input number of items in a combo (say N) and dollar amount (say X) via input dialog box. 
3. Also, user is asked to choose a category from the given dropdown list populated using Zappos API. Since, as of now information of all the Zappos products can not be extracted therefore categorywise API calls are used.
4. The program then extracts all the product names and prices under chosen category.
5. Based on these lists combinations of N items (per combinations) are formed and the total price of each is calculated and matched against X.
6. If total price is closer to X then combo is displayed.
