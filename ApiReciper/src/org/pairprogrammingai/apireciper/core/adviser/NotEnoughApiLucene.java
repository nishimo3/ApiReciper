package org.pairprogrammingai.apireciper.core.adviser;

import org.pairprogrammingai.apireciper.core.data.ApiInfo;
import org.pairprogrammingai.apireciper.core.data.SequenceInfo;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NotEnoughApiLucene {

    public static class ApiCountInfo {
        private ApiInfo apiInfo;
        private int count;

        public ApiCountInfo(ApiInfo _apiInfo){
            apiInfo = _apiInfo;
            count = 1;
        }

        public void countUp(){
            count++;
        }

        public String getStr(){
            return count + " " + apiInfo.getString() + "\n";
        }

        public void print(){
            System.out.print(count + ", ");
            apiInfo.println();
        }
    }

    public static class ApiCountInfoComparator implements Comparator<ApiCountInfo> {

        public int compare(ApiCountInfo s1, ApiCountInfo s2) {
            int v1 = s1.count;
            int v2 = s2.count;
            if (v1 < v2) {
                return 1;
            } else if (v1 == v2) {
                return 0;
            } else {
                return -1;
            }
        }
    }

    public NotEnoughApiLucene(){
    }

    public String execute(SequenceInfo seqInfo, List<SequenceInfo> searchResults){
        List<SequenceInfo> inResults = new ArrayList<SequenceInfo>();
        for(SequenceInfo result : searchResults){
            if(seqInfo.isInclude(result)){
                inResults.add(result);
            }
        }

        List<ApiCountInfo> apiCountInfoList = getApiKinds(inResults);
        return getPrintStr(apiCountInfoList);
    }

    private List<ApiCountInfo> getApiKinds(List<SequenceInfo> models){
        List<ApiCountInfo> apiCountInfos = new ArrayList<ApiCountInfo>();

        for(int i = 0; i < models.size(); i++){
            SequenceInfo seqInfo = models.get(i);
            List<ApiInfo> seq = seqInfo.getSequence();
            for(int j = 0; j < seq.size(); j++){
                ApiInfo apiInfo = seq.get(j);

                boolean findFlg = false;
                for(int k = 0; k < apiCountInfos.size(); k++){
                    ApiCountInfo apiCountInfo = apiCountInfos.get(k);
                    if(apiInfo.eq(apiCountInfo.apiInfo)){
                        apiCountInfo.countUp();
                        findFlg = true;
                        break;
                    }
                }
                if(!findFlg){
                    apiCountInfos.add(new ApiCountInfo(apiInfo));
                }
            }
        }

        // Sorting
        if(apiCountInfos != null){
            Collections.sort(apiCountInfos, new ApiCountInfoComparator());
        }
        return apiCountInfos;
    }

    private String getPrintStr(List<ApiCountInfo> apiKinds){
        String ret = "";
        int sum = 0;
        for(int i = 0; i < apiKinds.size(); i++){
            ApiCountInfo apiKind = apiKinds.get(i);
            sum = sum + apiKind.count;
        }

        DecimalFormat df = new DecimalFormat("##0.0%");
        for(int i = 0; i < apiKinds.size(); i++){
            ApiCountInfo apiKind = apiKinds.get(i);
            double p = (double)apiKind.count / (double)sum;
            ret = ret + "[" + df.format(p) + "]" + apiKind.apiInfo.getString() + "\n";
        }
        return ret;
    }
}
