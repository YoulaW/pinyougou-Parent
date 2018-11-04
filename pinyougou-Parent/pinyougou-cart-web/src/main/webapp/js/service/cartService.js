app.service("cartService",function($http){

    //获取购物车中的信息
    this.findCartList=function(){
        return $http.get('cart/findCartList.do');
    }

    //向购物车中添加商品
    this.addGoodsToCartList=function(itemId,num){
        return $http.get('cart/addGoodsToCartList.do?itemId='+itemId+'&num='+num);
    }

    //查询 checkBox选中的明细
    this.findSelect=function(ids){
        return $http.get('cart/findSelect.do?ids='+ids);
    }

    //根据checkBox 计算总金额
    //this.sum=function(orderItemList){
       // var totalValue ={totalNum:0,totalMoney:0.00 };//合计实体
        //遍历orderItemList
    //    for(var i=0;i<orderItemList.length;i++){
    //        totalValue.totalNum+=orderItemList[i].num;
     //       totalValue.totalMoney+=orderItemList[i].totalFee;
    //    }
    //    return totalValue;
   // }

    this.sum=function(cartList){
        var totalValue={totalNum:0, totalMoney:0.00 };//合计实体
        for(var i=0;i<cartList.length;i++){
            //var cart=cartList[i];
            for(var j=0;j<cartList[i].orderItemList.length;j++){
                var orderItem=cartList[i].orderItemList[j];//购物车明细
                totalValue.totalNum+=orderItem.num;
                totalValue.totalMoney+= orderItem.totalFee;
            }
        }
        return totalValue;
    }


    //确认提交订单
	this.submitOrder=function(order){
		return $http.post('order/add.do',order);
	}

})