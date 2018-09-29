// <!--定义模块-->
var app=angular.module('pinyougou',[]);

//定義過濾器  提供angularJs 的sce服務  轉義html樣式

app.filter('trustHtml',['$sce',function ($sce) {
    return function(data){
        return $sce.trustAsHtml(data);
    }
}]);