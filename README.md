# CodeCoverageAggregator

_Aggregate the code coverage for each Apex classes and triggers via Metadata API_

## Test & exec

1. Test
```
$ mvn test
```

2. Exce
```
$ mvn exec:java -Dexec.mainClass="cca.core.CodeCoverageAggregator"
```

3. Result
```
[Status]
Status is: InProgress | 2/19 (0 errors) | Running Test: CCASample001Test.testQueryAccount
Status is: InProgress | 5/19 (0 errors) | Running Test: CCASample002Test.testInsertAccount
Status is: InProgress | 8/19 (0 errors) | Running Test: CCASample003Test.testDeleteAccount
Status is: InProgress | 12/19 (0 errors) | Running Test: CCASample004Test.testDeleteAccount
Status is: InProgress | 15/19 (0 errors) | Running Test: CCASample004Test.testUpdateAccount
Status is: InProgress | 18/19 (0 errors) | Running Test: CCASample005Test.testUpdateAccount
Status is: Succeeded


[Result]
- DeployId       : 09SN0000000PzeqMAC
- Start Date     : 2014-02-08 09:23:51
- Completed Date : 2014-02-08 09:24:10
- Total time(ms) : 18083.0
- #Tests         : 19
- #Failures      : 0
- Status         : Succeeded
- Success?       : true


[Code Coverage]
ClassName               #CoveredLine  #ShouldCoverLine  %Coverage
------------------      ------------  ----------------  ---------
CCASample001                      17                21         80
CCASample002                      17                21         80
CCASample003                      17                21         80
CCASample004                      17                21         80
CCASample005                      14                21         66
------------------      ------------  ----------------  ---------
Overall coverage                  82               105         78
```

## License

Released under the [MIT Licenses](http://opensource.org/licenses/MIT)
