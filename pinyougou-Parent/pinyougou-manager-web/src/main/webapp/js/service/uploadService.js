app.service('uploadService',function ($http) {
    this.uploadImg=function () {

        var formData=new FormData();
        formData.append('file',file.files[0]);//固定寫法

        return $http({
            method:'POST',
            url:'.././uploadImg.do',
            data:formData,
            headers:{'Content-Type':undefined},//請求頭不能是默認的   默認的為json
            transformRequest:angular.identity
        });

        // anjularjs 对于 post 和 get 请求默认的 Content-Type header 是 application/json。
        // 通过设置 ‘Content-Type’: undefined，这样浏览器会帮我们把 Content-Type 设置为 multipart/form-data.
        //
        //     通过设置 transformRequest: angular.identity ，anjularjs transformRequest function 将序列化 我们的 formdata object.


    }
});