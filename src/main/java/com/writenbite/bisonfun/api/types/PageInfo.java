package com.writenbite.bisonfun.api.types;

public record PageInfo(
        Integer total,
        Integer perPage,
        Integer currentPage,
        Integer lastPage,
        Boolean hasNextPage
) {
    public static class PageInfoBuilder {
        private Integer total = 0;
        private Integer perPage = 0;
        private Integer currentPage = null;
        private Integer lastPage = 0;
        private Boolean hasNextPage = null;

        public PageInfoBuilder increaseTotal(Integer total) {
            this.total += total;
            return this;
        }

        public PageInfoBuilder setPerPage(Integer perPage) {
            this.perPage = perPage;
            return this;
        }

        public PageInfoBuilder setCurrentPageIfLess(Integer currentPage) {
            if(this.currentPage == null) {
                this.currentPage = currentPage;
            }else {
                this.currentPage = Math.min(this.currentPage, currentPage);
            }
            return this;
        }

        public PageInfoBuilder setLastPageIfGreater(Integer lastPage) {
            this.lastPage = Math.max(this.lastPage, lastPage);
            return this;
        }

        public PageInfoBuilder setHasNextPage(Boolean hasNextPage) {
            this.hasNextPage = hasNextPage;
            return this;
        }

        private boolean hasNextPage() {
            return hasNextPage != null ? hasNextPage : currentPage < lastPage;
        }

        public PageInfo createPageInfo() {
            return new PageInfo(total, perPage, currentPage, lastPage, hasNextPage());
        }
    }
}
