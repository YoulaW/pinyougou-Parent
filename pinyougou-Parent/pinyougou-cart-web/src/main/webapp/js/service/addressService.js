app.service('addressService',function ($http) {
    //根据用户查找收货明细
    this.findAddressByUser=function () {
        return $http.get('address/findAddressByUserName.do');
    }
    //根据addressId进行删除
    this.deleAddress=function (id) {
        return $http.get('address/delete.do?ids'+id);
    }
    //查找一个
    this.findOne=function (id) {
        return $http.get('address/findOne.do?id='+id);
    }

    //增加
    this.add=function(entity){
        return  $http.post('address/add.do',entity );
    }
    //修改
    this.update=function(entity){
        return  $http.post('address/update.do',entity );
    }

    //查看是否默认选中的地址
    this.isDefaultSelected=function () {
        return $http.get('address/isDefaultSelected.do');
    }

})