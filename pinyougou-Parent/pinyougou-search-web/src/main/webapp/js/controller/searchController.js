app.controller('searchController',function ($scope,$location,searchService) {




    //初始化搜索
    $scope.searchItems=function () {
        //修改 search 方法, 在执行查询前，转换为 int 类型，否则提交到后端有可能变成字符串
        $scope.searchMap.pageNo = parseInt($scope.searchMap.pageNo);

        searchService.searchItem($scope.searchMap).success(function (response) {
            $scope.resultMap=response;
            $scope.makePageLabel();//查询的同时  创建页码label
        })
    }

    //增加面包屑

    //定義一個變量  前端傳到後端的數據
    $scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':30,'sort':'','sortField':''};


    $scope.resultMap={};

    //定义一个点击查询的方法
    $scope.addSearchItem=function (key,value) {
        if(key=='brand'||key=='category'||key=='price'){
            $scope.searchMap[key]=value;
        }else {
            $scope.searchMap.spec[key]=value;
        }
        //新增之后重新 调用查询方法
        $scope.searchItems();
    }

    //定义 删除面包屑的方法
    $scope.deleSearchItem=function (key) {
        if(key=='category'){
            $scope.searchMap[key]='';//赋值为空
            $scope.searchMap={'keywords':$scope.searchMap.keywords,'category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':30,'sort':'','sortField':''};

        }else if (key=='brand'||key=='price'){
            $scope.searchMap[key]='';//赋值为空
        }
        else {
           delete $scope.searchMap.spec[key];//直接删掉
        }

        //移除之后重新 调用查询方法
        $scope.searchItems();
    }

    //获取要显示的页码  显示5条记录  其他隐藏
    $scope.makePageLabel=function () {
        $scope.pageLabel=[];
        //开始页
        var fristPage = 1;
        //结束页
        var lastPage = $scope.resultMap.totalPages;
        //省略号的显示
        $scope.firstDot=false;
        $scope.lastDot=false;


        if($scope.resultMap.totalPages>5){
            //大于5  以当前页为准
            if($scope.searchMap.pageNo<=3){
                lastPage=5;

                $scope.lastDot=true;

            }else if($scope.searchMap.pageNo>=$scope.resultMap.totalPages-2){
                lastPage=$scope.resultMap.totalPages;
                fristPage=lastPage-4;

                $scope.firstDot=true;

            }else {
                fristPage=$scope.searchMap.pageNo-2;
                lastPage=$scope.searchMap.pageNo+2;

                $scope.firstDot=true;
                $scope.lastDot=true;
            }
        }
        //将显示的页码 装到pageLabel集合中
        for(var i=fristPage;i<=lastPage;i++){
            $scope.pageLabel.push(i);
        }
    }
    //根据页码查询的方法
    $scope.searchByPage=function (currentPage) {
       //判断越界问题？？？
        if(currentPage<1||currentPage>$scope.resultMap.totalPages){
            return;
        }
        //将


        $scope.searchMap.pageNo=currentPage;
        $scope.searchItems();
    }
    //设置  上下页的 点击&隐藏   如果为第一页和最后一页不显示
    $scope.isTopPage=function () {
        if($scope.searchMap.pageNo==1){
            return false;
        }else{
            return true;
        }
    }

    $scope.isEndPage=function () {
        if($scope.searchMap.pageNo==$scope.resultMap.totalPages){
            return false;
        }else{
            return true;
        }
    }

    //根据条件升序或降序查询
    $scope.searchBySort=function (sort,sortField) {

        $scope.searchMap.sort=sort;
        $scope.searchMap.sortField=sortField;
        $scope.searchItems();
    }

    $scope.searchHaveBrand=function () {
        var list = $scope.resultMap.brandList;
        if($scope.searchMap.keywords!=null) {
            // for (var i= 0;i<list.length;i++){
            for (var i = 0; i < $scope.resultMap.brandList.length; i++) {
                // alert(list[i].text)

                // if($scope.searchMap.keywords.indexOf(list[i].text)>=0){
                if ($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text) >= 0) {
                    return false;
                }
            }
        }
            // alert(2)
        return true;
    }

    //接受portal 跳转的页面  接受请求参数 并封装到查询对象中  页面加载时初始化方法
    $scope.localKeywords=function () {
        $scope.searchMap.keywords=$location.search()['keywords'];
        $scope.searchItems();
    }


})