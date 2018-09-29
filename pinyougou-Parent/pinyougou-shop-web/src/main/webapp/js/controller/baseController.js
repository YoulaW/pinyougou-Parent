 //品牌控制层 
app.controller('baseController' ,function($scope){	
	
    //重新加载列表 数据
    $scope.reloadList=function(){
    	//切换页码  
    	$scope.search( $scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);	   	
    }
    
	//分页控件配置 
	$scope.paginationConf = {
         currentPage: 1,
         totalItems: 10,
         itemsPerPage: 10,
         perPageOptions: [10, 20, 30, 40, 50],
         onChange: function(){
        	 $scope.reloadList();//重新加载
     	 }
	}; 
	
	$scope.selectIds=[];//选中的ID集合 

	//更新复选
	$scope.updateSelection = function($event, id) {		
		if($event.target.checked){//如果是被选中,则增加到数组
			$scope.selectIds.push( id);			
		}else{
			var idx = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(idx, 1);//删除 
		}
	}

	//json转换为字符串
	$scope.jsonToString=function(jsonString,key){
		var json = JSON.parse(jsonString);
		var value ="";

		for(var i=0;i<json.length;i++){
			if(i>0){
				value+=",";
			}
			value+=json[i][key];
		}

		return value;
	}

	//定義被選中 后  集合中是否包含name值
	$scope.selectIsOld=function (list,oldName,selctName) {

		// if (list!=null&&list.length>0){//判斷集合 是否爲空
			//便利集合
			for(var i=0;i<list.length;i++){
				if(list[i][oldName]==selctName){
					return list[i];
				}

				// alert(list.length+"spec長度")

			}

		// }
		return null;
    }


	
});	