/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package organization.purge;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.cleanup.service.ApiPurge;
import org.powermock.api.mockito.PowerMockito;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.GatewayArtifactsMgtDAO;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.persistence.APIPersistence;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ServiceReferenceHolder.class, APIPersistence.class, ApiMgtDAO.class,
        GatewayArtifactsMgtDAO.class, APIUtil.class })
public class ApiPurgeTest {

    private ApiMgtDAO apiMgtDAO;
    private GatewayArtifactsMgtDAO gatewayArtifactsMgtDAO;
    private ServiceReferenceHolder serviceReferenceHolder;

    @Before public void init() {
        apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        gatewayArtifactsMgtDAO = Mockito.mock(GatewayArtifactsMgtDAO.class);
        serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
    }

    @Test public void testOrganizationRemoval() throws APIManagementException {

        PowerMockito.mockStatic(ApiMgtDAO.class);
        PowerMockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);

        PowerMockito.mockStatic(GatewayArtifactsMgtDAO.class);
        PowerMockito.when(GatewayArtifactsMgtDAO.getInstance()).thenReturn(gatewayArtifactsMgtDAO);

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);

        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        APIManagerConfigurationService apiManagerConfigurationService = Mockito
                .mock(APIManagerConfigurationService.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).
                thenReturn(apiManagerConfigurationService);

        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);

        PowerMockito.mockStatic(APIUtil.class);
        Mockito.when(APIUtil.isAllowDisplayAPIsWithMultipleStatus()).thenReturn(true);

        APIIdentifier api = Mockito.mock(APIIdentifier.class);
        ArrayList<APIIdentifier> apiIdentifierList = new ArrayList<>();
        apiIdentifierList.add(api);

        Mockito.doReturn(apiIdentifierList).when(apiMgtDAO).getAPIIdList("testOrg");
        Mockito.doNothing().when(apiMgtDAO).deleteOrganizationAPIList(Mockito.any());
        Mockito.doNothing().when(gatewayArtifactsMgtDAO).removeOrganizationGatewayArtifacts(Mockito.any());

        ApiPurge apiPurge = new ApiPurge("test-username");

        LinkedHashMap<String, String> subtaskResult =   apiPurge.deleteOrganization("testOrg");
        for(Map.Entry<String, String> entry : subtaskResult.entrySet()) {
            Assert.assertEquals(entry.getKey() + " is not successful",
                    APIConstants.OrganizationDeletion.COMPLETED, entry.getValue());
        }

        Mockito.verify(apiMgtDAO, Mockito.times(1)).getAPIIdList("testOrg");
        Mockito.verify(apiMgtDAO, Mockito.times(1)).deleteOrganizationAPIList(Mockito.any());
        Mockito.verify(gatewayArtifactsMgtDAO, Mockito.times(1)).
                removeOrganizationGatewayArtifacts(Mockito.any());
    }
}