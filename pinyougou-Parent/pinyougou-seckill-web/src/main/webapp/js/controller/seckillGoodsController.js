 //控制层 
app.controller('seckillGoodsController' ,function($scope,$controller,$location,$interval,seckillOrderService,payService ,seckillGoodsService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		seckillGoodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		seckillGoodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		seckillGoodsService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=seckillGoodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=seckillGoodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	$scope.reloadList();//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		seckillGoodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		seckillGoodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

	//查找有效的 秒杀商品列表
	$scope.findSecKillList=function () {
		seckillGoodsService.findSecKillList().success(function(response){
			$scope.seckillList = response;
		})
    }

    //-----------------秒杀的商品详情
		//获取秒杀商品的id
    // $scope.getSeckillGoodsId=function () {
		// return $location.search()['id'];
    // }

    //单个商品的详细页

	$scope.findOneById=function () {
		seckillGoodsService.findOneById($location.search()['id']).success(function (response) {
			$scope.seckillGoods=response.seckillGoods;//response.currentTime 服务器系统时间

			//设置计时器
			allSeconds=Math.floor((new Date($scope.seckillGoods.endTime).getTime()
			-(new Date(response.currentTime ).getTime()))/1000);//获取距离结束的总秒数  由时间毫秒值相减 转为秒

			time=$interval(function () {
				if (allSeconds>0){
					allSeconds=allSeconds-1;
					$scope.timeString=convertTimeString(allSeconds);//转换时间字符串
                }else {
					$interval.cancel(time);
					alert("秒杀结束")
				}
            },1000)
			//$interval(执行的函数,间隔的毫秒数,运行次数);
			//$interval.cancel(time)  取消执行用 cancel 方法
        })
    }
    //将秒转换为字符串
    convertTimeString=function(allSeconds){
		var days= Math.floor(allSeconds/24*60*60);//天数
		var hours=Math.floor((allSeconds-days*60*6024)/60*60);//小时
        var minutes= Math.floor( (allSeconds -days*60*60*24 - hours*60*60)/60 );//分钟数
        var seconds= allSeconds -days*60*60*24 - hours*60*60 -minutes*60; //秒数
		var timeStr="";
		if (days>0){
			timeStr=days +" 天 ";
		}
		timeStr+= hours+" 时 "+minutes+" 分 " + seconds +" 秒 ";
		return timeStr;
	}

	//===========订单的生成=======
	$scope.submitOrder=function () {
		alert($location.search()['id']);
        seckillOrderService.submitOrder($location.search()['id']).success(function (response) {
			if(response.success){//订单成功  跳到支付页面
                alert("下单成功，请在 1 分钟内完成支付");
                location.href="pay.html"
			}else {
				alert(response.message);
			}
        })
    }
	//==============生成二维码---------

	$scope.createNative=function () {
		alert(1111111)
		payService.createNative().success(function(response) {
			// alert(22222)

            $scope.map = response;
			// alert(333333)
			// alert($scope.map.getEntries());
            //二维码  遍历生成
            for (var k in $scope.map){
                createQ($scope.map[k].code_url)

			}
        }).error("错误")
    }
    $scope.payMessage="";
    queryOrderPayStatus=function (orderId) {
       payService.queryOrderPayStatus(orderId).success(function (response) {
           if (response.message){//成功
               //location.href="paysuccess.html#?money="+$scope.totalManey;
               // location.href="paysuccess.html#?money="+$scope.totalManey;
			   $scope.payMessage="支付成功！";
           }else {//失败
               if(response.message.equals('二维码超时')){//重新生成二维码
				   $scope.payMessage="二维码超时";
                   // $scope.createNative();
               }else {
                   // location.href="payfail.html";
                   $scope.payMessage="支付失败！";
               }
           }
       })
    }

    createQ=function (id) {
    	alert(333333)
		alert(id)
        var qr = new QRious({
            element:document.getElementsByName('erweima'+id)[0],
            size:250,
            level:'H',
            value:id
        })
		alert(qr.element)
		alert(qr.size)
    }


});	
