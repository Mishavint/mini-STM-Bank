# What is it?
It is web bank, where you can keep your money

## Rest api
If you want to get info about your account, you have to call Get user

If you want to add new user, you have to call Post user

If you want to refill/outdraw money from your account, you have to call Patch user

If you want to make transfer to somebody, you have to call Patch api

### Get user
``curl -i -X GET -H 'Content-Type: application/json' -d 'PASSWORD' http://.../api/ACCOUTN NUBMER``
### Post user
``curl -i -X POST -H 'Content-Type: application/json' -d '{"name":"NAME","password":"PASSWORD"}' http://.../api ``
### Patch user
``curl -i -X PATCH -H 'Content-Type: application/json' -d '{"amount":AMMOUNT,"password":"PASSWORD"}' http://.../api/ACCOUNT NUMBER ``
### Patch api
``curl -i -X PATCH -H 'Content-Type: application/json' -d '{"number1":"ACCOUNT NUMBER","number2":"ACCOUNT NUMBER","amount":AMOUNT"password":"PASSWORD"}' http://.../api``

## Test coverage
Test coverage can be found in GitHub actions/Test with coverage/last workflow
You can download coverage reports or look into test/JaCoCo Coverage Report