 //控制层 
app.controller('itemPageController' ,function($scope,$http){
	
	//购买数量的计算
	$scope.num=1;
	$scope.addNum=function(x){
		$scope.num+=x;
		if($scope.num<1){
			$scope.num=1;
		}
	}
	
   //商品spec选择
    //[{"attributeValue":["移动3G"],"attributeName":"网络"},
   //{"attributeValue":["16G"],"attributeName":"机身内存"}]
   $scope.specList={};
   $scope.selectSpec=function(key,value){
	   if(value!=null){
		   $scope.specList[key]=value;
		   //读取SKU，每次选择改变都要重新加载 赋值SKU
		   searchSKU();
	   }
   }
   //确认商品是否有选中，显示被选中的样式
   $scope.isSelectSpec=function(key,value){
	   if($scope.specList[key]==value){
		   return true;
	   }
	   return false;
   }
   
   //页面用freemarker先初始化  SKU   默认选中的 赋值给一个变量
   $scope.sku={};
	$scope.loadSKU=function(){
		$scope.sku=skuList[0];
		$scope.specList=JSON.parse(JSON.stringify($scope.sku.spec));
	}
	
	//对比两个map对象 是否相等   分别比较  若相等返回true
	matchObject=function(map1,map2){
		//从map1找map2
		for(var k in map1){
			if(map1[k]!=map2[k]){
				return false;
			}
		}
		//从map2中找map1
		for(var k in map2){
			if(map2[k]!=map1[k]){
				return false;
			}
		}
		//找的到就返回true
		return true;
		
	}
	//查看SKU的明细 是否存在   在选择规格的时候调用check下 每次更新都要调用
	searchSKU=function(){
		//遍历skuList
		for(var i=0;i<skuList.length;i++){
			if(matchObject(skuList[i].spec,$scope.specList)){
				$scope.sku=skuList[i];
				//找到就返回
				return;
			}			
		}
		//没找到就是没有该spec  重新设置
		$scope.sku={id:0,title:'--------',price:0};//如果没有匹配的
	}
	
	//加载到购物车
	$scope.addToCat=function(){
		//alert('skuid'+$scope.sku.id);
		//跳到购物车 的addGoodsCart中  成功后跳转到cart.html购物车页面
		//失败弹框
		//{'withCredentials':true} 为了解决跨域问题  必须要携带访问
		$http.get('http://localhost:9107/cart/addGoodsToCartList.do?itemId='+$scope.sku.id+
			'&num='+$scope.num,{'withCredentials':true}).success(
			function (response) {
				if(response.message){//成功跳转到cart.html
					location.href="http://localhost:9107/cart.html";
				}else {
					alert(response.message);
				}
            }
		)


	}
	
	

});	
