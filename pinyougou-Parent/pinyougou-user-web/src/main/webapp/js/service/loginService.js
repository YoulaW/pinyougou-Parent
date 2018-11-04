
app.service("loginService",function ($http) {
    //获取登陆的 用户名
    this.showName=function () {
        return $http.get("../login/name.do");
    }
})