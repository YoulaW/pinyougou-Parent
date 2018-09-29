 //控制层 
app.controller('contentController' ,function($scope,contentService){

	//輪播圖集合
	$scope.contentCategoryList=[];

	$scope.findContentCategory=function (categoryId) {
        contentService.findByCategoryId(categoryId).success(function(response){

                $scope.contentCategoryList[categoryId]=response;
		})
    }
    //跳转到search 搜索页
    $scope.search=function () {
        location.href="http://localhost:9104/search.html#?keywords="+$scope.keywords;
    }



});
