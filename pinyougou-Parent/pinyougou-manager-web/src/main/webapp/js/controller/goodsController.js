 //控制层 
app.controller('goodsController' ,function($scope,$controller,$location,itemCatService,typeTemplateService,goodsService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}
		);
	}
    $scope.itemCatList=[];
    //便利goods中 商品級別  然 後查詢tb_item_cat 中findOnebyId  取name賦值給goods. category1_id 變量
    //讀取商品列表  >{{itemCatList[entity.category1Id]}}<  將id轉換為name顯示
	$scope.checkItemCat=function(){
		itemCatService.findAll().success(function(response){
			//遍歷 response  存入數組中 將id存為下標   name村為 下標對應的值
			for(var i=0;i<response.length;i++){
				$scope.itemCatList[response[i].id]=response[i].name;
			}
		})
	}





    //分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
                $scope.list=response.rowMessage;
                $scope.paginationConf.totalItems=response.totalPage;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(){
		var id=$location.search()['id'];

		if(id==null){
			return;
		}

		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;

                //商品介紹
                editor.html($scope.entity.goodsDesc.introduction);
                //獲取圖片
                $scope.entity.goodsDesc.itemImages=JSON.parse($scope.entity.goodsDesc.itemImages);
                //獲取擴展屬性
                $scope.entity.goodsDesc.customAttributeItems=JSON.parse($scope.entity.goodsDesc.customAttributeItems);
                //獲取規格
                $scope.entity.goodsDesc.specificationItems=JSON.parse($scope.entity.goodsDesc.specificationItems);
                //轉換sku中規格選項  就是將item中spec 轉為JSON對象  然後再從前臺遍歷
				for(var i = 0;i<$scope.entity.items.length;i++){
					$scope.entity.items[i].spec=JSON.parse($scope.entity.items[i].spec);

				}


            }
		);				
	};

	//回顯時讀取  是否選中   指令 ng-checked=“true or false”
	//定義
	$scope.checkAttributeValue=function (specName,option) {
        var items= $scope.entity.goodsDesc.specificationItems;
        var object=$scope.selectIsOld(items,'attributeName',specName);
        if(object==null){
        	return false;
		}else{
        	if(object.attributeValue.indexOf(option)>=0){
        		return true;
			}else {
        		return false;
			}
		}

    }







	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象

		if($scope.entity.goods.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
            //<!--提取 kindeditor 编辑器的内容-->
            $scope.entity.goodsDesc.introduction=editor.html();
			// alert(editor.html());
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
                    // $scope.reloadList();//重新加载
					//保存成功后 請空内容 以便下次編寫
                    // alert(response.message);
                    // $scope.entity={};
                    // editor.html('');//清空富文本编辑器
                    // $scope.entity={goodsDesc:{itemImages:[]}};
					location.href="goods.html";//跳转到商品列表页
				}else{
					alert(response.message);
				}
			}		
		).error(function () {
            alert("保存&更新發生錯誤")
        });

	};




    // $scope.add=function() {
    //     //<!--提取 kindeditor 编辑器的内容-->
    //     $scope.entity.goodsDesc.introduction=editor.html();
    //
    //     goodsService.add( $scope.entity).success(function(response){
    //             if(response.success){
    //                 //重新查询
    //                 $scope.reloadList();//重新加载
    //                 //保存成功后 請空内容 以便下次編寫
    //                 $scope.entity={};
    //                 editor.html('');//清空富文本编辑器
    //                 $scope.entity={goodsDesc:{itemImages:[]}};
    //             }else{
    //                 alert(response.message);
    //             }
    //         }
    //     );
    //
    //
    // };

	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}else {
					alert(response.message);
				}
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
                $scope.list=response.rowMessage;
                $scope.paginationConf.totalItems=response.totalPage;//更新总记录数
			}			
		);
	}


	//保存圖片list
    $scope.entity={goods:{},goodsDesc:{itemImages:[],specificationItems:[]}};

	$scope.saveImg=function () {
        $scope.entity.goodsDesc.itemImages.push($scope.img_entity);
    }

    //刪除圖片
    $scope.deleImg=function (index) {
        $scope.entity.goodsDesc.itemImages.splice(index,1);
    }


    //圖片上傳
    $scope.upload_Img=function () {
        uploadService.uploadImg().success(function(response){
			//判斷是否成功
			if (response.success){//如果上传成功，取出 url
                $scope.img_entity.url=response.message;//设置文件地址
			}else {
				alert(response.message);
            }
		}).error(function () {
			alert("上傳發生錯誤")
        })
    }


    //查詢商品分類  一級目錄  就是parentId=0   將選擇的變量封裝在goods表中的category1Id中

	$scope.findItemCat=function(parentId){
        itemCatService.findByParentId(parentId).success(function(response){
            $scope.itemList= response;
		})
	}

	// 讀取二級目錄 監管一級變量 entity.goods.category1Id  如果改變自動觸發函數
	$scope.$watch('entity.goods.category1Id',function(newValue,oldValue){
		//判斷 一級變量的newValue 是否有定義，如無定義 不觸發函數  定義再觸發  否則影響 3級數據
		if(oldValue!=undefined){
            $scope.entity.goods.category2Id=-1; //給一個無效的值
		}

		if(newValue!=undefined){
			// alert(newValue+"1ID xin")
            itemCatService.findByParentId(newValue).success(function(response){
            $scope.itemList1=response;
            })
		}
	})

    // 讀取三級目錄 監管二級變量 entity.goods.category2Id  如果改變自動觸發函數
    $scope.$watch('entity.goods.category2Id',function(newValue,oldValue){
        //判斷 二級變量的newValue 是否有定義，如無定義 不觸發函數  定義再觸發  否則影響 模板數據
        if(oldValue!=undefined){
            $scope.entity.goods.category3Id=-1; //給一個無效的值
        }

        if(newValue!=undefined){
            // alert(newValue+"2ID xin")
            itemCatService.findByParentId(newValue).success(function(response){
                $scope.itemList2=response;
            })
        }
    })

    // 讀取模板   監管三級變量 entity.goods.category3Id  如果改變自動觸發函數   存放在goods中typeTemplateId中
    $scope.$watch('entity.goods.category3Id',function(newValue,oldValue){
        //判斷 三級變量的newValue 是否有定義，如無定義 不觸發函數  定義再觸發  否則影響 模板數據
        if(oldValue!=undefined){
            $scope.entity.goods.typeTemplateId=-1; //給一個無效的值
        }



        if(newValue!=undefined){
            // alert(newValue+"3ID xin")
            itemCatService.findOne(newValue).success(function(response){
                $scope.entity.goods.typeTemplateId=response.typeId;
			})
        }
    })

    // 讀取品牌   監管模板變量 entity.goods.typeTemplateId 如果改變自動觸發函數

    $scope.$watch('entity.goods.typeTemplateId',function(newValue,oldValue){
        //判斷 模板變量的newValue 是否有定義，如無定義 不觸發函數  定義再觸發  否則影響 模板數據

        if(oldValue!=undefined){ //如果模板ID改變前為 undefined 將模板展示的所有變量都清空
            $scope.specificationList=[]; //給一個無效的值
            $scope.entity.goodsDesc.specificationItems=[];
            $scope.entity.items=[];
            $scope.entity.goods.isEnableSpec='0';
        }



        if(newValue!=undefined&&newValue!=-1){

            typeTemplateService.findOne(newValue).success(function(response){
            	$scope.typeTemplate=response;
            	//將typeTemplate.brandIds 轉爲json數組對象

                // 品牌  從 數據庫tb_type_template  中brand_ids 字段  存放在goods中brandId中
            	$scope.typeTemplate.brandIds=JSON.parse($scope.typeTemplate.brandIds);
            	// alert($scope.typeTemplate.brandIds);
				// 擴展屬性 tb_type_template  中  custom_attribute_items   后 存放在  tb_goods_desc  中的custom_attribute_items
                if($location.search()['id']==null) {
                    $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems);
                }

            })

            typeTemplateService.findTempSpec(newValue).success(function(response){
                // 規格讀取tb_type_template  中  specification_items     后 存放在  tb_goods_desc  中的specification_items
                // 再查詢 option表  將 option也加入
                //改造 TbTypeTemplate findOne   option也加入到specIds中
				// response 為List<Map> specIds:[{ids:27,'text':wang,options:[{id:,optionName,specId:,orders:..}]}]

                $scope.specificationList=response;

                $scope.specificationList=JSON.parse($scope.specificationList);

               // alert("========"+$scope.goodsDesc.specificationItems);
			})

        }



    })



	//規格的選擇
	$scope.selectSpec=function($event,rowName,rowValue){
		//將初始化值  付給list
		// var list =$scope.entity.goodsDesc.specificationItems;
		//調用方法  并將返回結果賦值給object
        var object = $scope.selectIsOld($scope.entity.goodsDesc.specificationItems,"attributeName",rowName);

        if(object!=null){//不爲空 直接添加
			// alert($event.target.checked);
			//判斷點擊是 增加還是取消
			if($event.target.checked){//增加  被選中
                object.attributeValue.push(rowValue);
			}else {//取消
				object.attributeValue.splice(object.attributeValue.indexOf(rowValue),1);
				//判斷 value的長度  為0則 移除name
				if(object.attributeValue.length==0){
                    $scope.entity.goodsDesc.specificationItems.splice(
                    	$scope.entity.goodsDesc.specificationItems.indexOf(object),1);
				}

			}
		}else {//為空 重新創建
			// alert(object!=null)
            $scope.entity.goodsDesc.specificationItems.push({"attributeName":rowName,"attributeValue":[rowValue]});
		}
	}





	//定義一個創建點擊的方法  傳參數 是當前的元數據當前的  Attr name  AttrValue

    $scope.createRow=function(){
        //定義初始化的 行信息
        $scope.entity.items=[{spec:{},price:0,num:99999,status:'0',isDefault:'0'}];

        //遍歷 $scope.entity.goodsDesc.specificationItems 添加行
		var list = $scope.entity.goodsDesc.specificationItems;

		for(var i=0;i<list.length;i++){

            $scope.entity.items=$scope.addRow($scope.entity.items,list[i].attributeName,list[i].attributeValue);
		}


    };


    //創建 每一行信息
	$scope.addRow=function(list,name,value){
		var newList=[];

		//遍歷list
		for (var i=0;i<list.length;i++){
			var oldList=list[i];

			for (var j=0;j<value.length;j++){
				 var newRow=JSON.parse(JSON.stringify(oldList));//拿到克隆后的新行 集合
				 newRow.spec[name]=value[j];
				 newList.push(newRow);
			}
		}
		return newList;
	}

	//讀取狀態
	$scope.rStatus=['未审核','已审核','审核未通过','关闭'];//商品狀態

    $scope.$watch('entity.goods.isEnableSpec',function (newValue,oldValue) {

        if(newValue!='1'){

            $scope.entity.goodsDesc.specificationItems=[];
            $scope.entity.items=[];

        }
    })

	//駁回/審核通過的方法  修改goods中status的狀態
	$scope.updateStatus=function(auditStatus){
        // goodsService.updateStatus($scope.selectIds,$scope.goods.auditStatus).success(function(response){
		goodsService.updateStatus($scope.selectIds,auditStatus).success(function(response){
			if(response.success){
                $scope.reloadList();//刷新列表
                $scope.selectIds=[];//清空 ID 集合
			}else {
				alert(response.message);
			}
		})
	}


});	
