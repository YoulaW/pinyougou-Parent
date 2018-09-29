app.service('searchService',function ($http) {

    this.searchItem=function (searchMap) {
        return $http.post('itemSearch/search.do',searchMap);
    }


})