app.controller('cartController',function($scope,$controller,$location,addressService,payService,cartService){


    $controller('baseController',{$scope:$scope});//继承
    $scope.loginName="";

    //页面初始化  加载方法  获取购物车中的信息  sellerid name  以及 order Itemlist
    $scope.findCartList=function () {
        cartService.findCartList().success(function (response) {
            $scope.cartList=response.cartList;
            $scope.loginName=response.loginName;
            // alert($scope.selectIds.length)
            //清空选中的商品
            $scope.selectIds=[];

            // $scope.selectMoney();//查询选中商品的金额  ？？？？不起作用？？？

        })
    }

    //向购物车中添加商品
    $scope.addGoodsToCartList=function (itemId,num) {
        cartService.addGoodsToCartList(itemId,num).success(function (response) {
            if (response.message){
                $scope.findCartList();//刷新
            }else {
                alert(response.message);
            }

        })
    }
    //选中商品的 总金额
    // $scope.cartListSelect=[];
    $scope.selectMoney=function () {
        cartService.findSelect($scope.selectIds).success(function (response) {
            $scope.cartListSelect=response;
            $scope.totalValue= cartService.sum($scope.cartListSelect);
            // alert($scope.totalValue.totalNum+" totalvalue.totalNum");
        })
    }
    //结算  购物车物品
    $scope.buyCart=function () {
        if($scope.selectIds.length>0){
            location.href="getOrderInfo.html#?selectIds="+$scope.selectIds;
        }else {
            alert("请选择需要购买的商品")
        }
    };




    //生成订单时获取被选中的ids  在getOrderInfo页面初始化的时候执行
    $scope.selectItems=function () {
        $scope.selectIds=$location.search()['selectIds'];
        // alert($scope.selectIds+"ids");
        // alert($scope.selectIds.length+"ids.length");
        $scope.selectMoney();
        $scope.selectIds=[];//将选中的清空
    }

    //添加数量改变时 同步更新到购物车中
    $scope.goodsNumChange=function (itemId,num) {
        if(num<=0){
            num=1;
        }
        $scope.addGoodsToCartList(itemId,num);

    }
    //判断用户是否有登陆
    $scope.isLogin=function(){
        if ($scope.loginName=='anonymousUser'){
            return false;
        }
        return true;
    }
    //判断是否选中  遍历$scope.selectIds  如果itemId相等则返回true
    $scope.isSelect=function(itemId){
        alert($scope.selectIds.length);
        if ($scope.selectIds.length>0){
            for(var i = 0;i<$scope.selectIds.length;i++){
                if($scope.selectIds[i]==itemId){
                    return true;
                }
            }
            return false
        }
        return false;
    }


    //------------对收货地址的操作-------------------
    //获取用户的收货地址
    $scope.findAddressByUser=function () {
        addressService.findAddressByUser().success(function (response) {
            $scope.addressList=response.addressList;
            $scope.loginName=response.loginName;
        })
    }


    //查询实体
    $scope.findOne=function(id){
        addressService.findOne(id).success(
            function(response){
                $scope.address= response;
            }
        );
    }

    //保存
    $scope.save=function(){
        $scope.address.userId=$scope.loginName;
        alert($scope.loginName+"  loginName");
        var serviceObject;//服务层对象
        if($scope.address.id!=null){//如果有ID
            serviceObject=addressService.update( $scope.address ); //修改
        }else{
            serviceObject=addressService.add( $scope.address );//增加
        }
        serviceObject.success(
            function(response){
                if(response.success){
                    //重新查询
                    $scope.findAddressByUser();//重新加载
                }else{
                    alert(response.message);
                }
            }
        );
    }


    //批量删除
    $scope.dele=function(id){
        //获取选中的复选框
        addressService.deleAddress( id ).success(
            function(response){
                if(response.success){
                    $scope.findAddressByUser();//刷新列表

                }
            }
        );
    }


    $scope.addressAlias=function ($event) {
        $scope.address.alias=$event.target.innerText;
        alert($scope.address.alias+"  $event.innerText")
    }

    // 点击选中地址的方法
    $scope.selectAddress=function(address){

        $scope.address=address;
    }
    //判断地址是否被选中
    $scope.isSelectAddress=function(address){
        if($scope.address==address){
            return true;
        }else {
            return false;
        }
    }

    //页面初始化的时候加载  该地址是否是默认地址   状态为1的
    $scope.isDefaultSelected=function () {
        addressService.isDefaultSelected().success(function(response){
            $scope.address=response;
        })
    }


    //-------------结束-------------

    $scope.order={paymentType:1};
    //-------------订单相关---------
    $scope.selectPayType=function (payType) {
        $scope.order.paymentType=payType;
    }
    
    
    
    //----------------------提交订单---------
    
    $scope.submitOrder=function () {
        $scope.order.receiverAreaName=$scope.address.address;//地址
        $scope.order.receiverMobile=$scope.address.mobile;//手机
        $scope.order.receiver=$scope.address.contact;//联系人

        cartService.submitOrder($scope.order).success(function (response) {

            if(response.success){
                //页面跳转
                if($scope.order.paymentType=='1'){//如果是微信支付，跳转到支付页面
                    location.href="pay.html#?total_fee="+$scope.totalValue.totalMoney;
                    // location.href="pay.html#?total_fee="+"0.01";
                }else{//如果货到付款，跳转到提示页面
                    location.href="paysuccess.html";
                }
                $scope.selectIds=[];//选中的ID集合 清空


            }else{
                alert(response.message); //也可以跳转到提示页面
            }

        })

    }


    //-----------------支付----------------
    //生成二维码
    $scope.createNative=function(){
        //获得订单金额
        $scope.total_fee =$location.search()['total_fee'];

        payService.createNative().success(function (response) {
            alert(response.total_fee+"  total_fee");
            $scope.totalManey=(response.total_fee/100).toFixed(2);  //获取得到的是分  要转为元 保留2位小数
            $scope.out_trade_no=response.out_trade_no;//订单
            alert(response.code_url)
            //二维码
                var qr = new QRious({
                element:document.getElementById('qrious'),
                size:250,
                level:'H',
                value:response.code_url
            });
            //调用支付状态
            queryOrderPayStatus(response.out_trade_no);
        })

    }
    //查看支付状态
    queryOrderPayStatus=function (out_trade_no) {
        payService.queryOrderPayStatus(out_trade_no).success(function (response) {
            if (response.message){//成功
                //location.href="paysuccess.html#?money="+$scope.totalManey;
                location.href="paysuccess.html#?money="+$scope.totalManey;
            }else {//失败
                if(response.message.equals('二维码超时')){//重新生成二维码
                    $scope.createNative();
                }else {
                    location.href="payfail.html";
                }
            }


        })
    }

    //成功页面获得money
    $scope.getMoney=function () {
        $scope.totalManey=$location.search()['money'];
    }



})