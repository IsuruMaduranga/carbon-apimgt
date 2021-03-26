/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import React, { useState } from 'react';
import Accordion from '@material-ui/core/ExpansionPanel';
import AccordionDetails from '@material-ui/core/ExpansionPanelDetails';
import AccordionSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import Typography from '@material-ui/core/Typography';
import AccordionActions from '@material-ui/core/ExpansionPanelActions';
import Button from '@material-ui/core/Button';
import CopyToClipboard from 'react-copy-to-clipboard';
import TextField from '@material-ui/core/TextField';
import Alert from 'AppComponents/Shared/Alert';
import { makeStyles } from '@material-ui/core/styles/index';
import { FormattedMessage } from 'react-intl';
import Grid from '@material-ui/core/Grid';

const useStyles = makeStyles((theme) => (
    {
        bootstrapRoot: {
            padding: 0,
            'label + &': {
                marginTop: theme.spacing(1),
            },
        },
        bootstrapCurl: {
            borderRadius: 4,
            backgroundColor: theme.custom.curlGenerator.backgroundColor,
            color: theme.custom.curlGenerator.color,
            border: '1px solid #ced4da',
            padding: '5px 12px',
            marginTop: '11px',
            marginBottom: '11px',
            width: '100%',
            transition: theme.transitions.create(['border-color', 'box-shadow']),
            '&:focus': {
                borderColor: '#80bdff',
                boxShadow: '0 0 0 0.2rem rgba(0,123,255,.25)',
            },
            fontSize: 12,
            fontFamily: 'monospace',
            fontWeight: 600,
        },
        subscriptionSummary: {
            backgroundColor: theme.custom.AsyncTryOut.backgroundColor,
            maxHeight: '40px',
            borderColor: '#80bdff',
            '&$expanded': {
                maxHeight: '40px',
            },
        },
        subscription: {
            paddingBottom: '10px',
        },
    }
));

export default function GenericSubscriptionUI(props) {
    const classes = useStyles();
    const { generateGenericSubscriptionCommand, topic } = props;
    const [command, setCommand] = useState(generateGenericSubscriptionCommand(topic));

    const handleClick = () => {
        setCommand(generateGenericSubscriptionCommand(topic));
    };

    return (
        <Accordion className={classes.subscription}>
            <AccordionSummary
                expandIcon={<ExpandMoreIcon />}
                aria-controls='generic-subscription-content'
                id='generic-subscription-header'
                className={classes.subscriptionSummary}
            >
                <Typography>{topic.name}</Typography>
            </AccordionSummary>
            <AccordionDetails>
                <Grid container direction='column' wrap='nowrap'>
                    <TextField
                        label='cURL'
                        defaultValue=''
                        value={command}
                        multiline
                        InputProps={{
                            disableUnderline: true,
                            classes: {
                                root: classes.bootstrapRoot,
                                input: classes.bootstrapCurl,
                            },
                        }}
                        InputLabelProps={{
                            shrink: true,
                            className: classes.bootstrapFormLabel,
                        }}
                    />
                </Grid>
            </AccordionDetails>
            <AccordionActions style={{ paddingRight: '18px' }}>
                <Button size='small' onClick={handleClick}>
                    <FormattedMessage id='Apis.Details.AsyncApiConsole.Curl' defaultMessage='Generate Curl' />
                </Button>
                <CopyToClipboard
                    text={command}
                    onCopy={() => Alert.info(
                        <FormattedMessage id='Apis.Details.AsyncApiConsole.Copied' defaultMessage='cURL copied' />,
                    )}
                >
                    <Button size='small'>
                        <FormattedMessage id='Apis.Details.AsyncApiConsole.Copy' defaultMessage='Copy Curl' />
                    </Button>
                </CopyToClipboard>
            </AccordionActions>
        </Accordion>
    );
}
