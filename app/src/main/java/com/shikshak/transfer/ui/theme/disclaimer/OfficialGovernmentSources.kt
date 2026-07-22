package com.shikshak.transfer.ui.theme.disclaimer

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shikshak.transfer.R

@Composable
fun OfficialGovernmentSources(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sources = listOf(
        stringResource(R.string.official_source_state_portal) to stringResource(R.string.url_state_bihar),
        stringResource(R.string.official_source_edu_online) to stringResource(R.string.url_edu_online),
        stringResource(R.string.official_source_education_dept) to stringResource(R.string.url_education_bih),
        stringResource(R.string.official_source_scert) to stringResource(R.string.url_scert_bihar),
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.official_sources_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.official_sources_intro),
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        sources.forEach { (label, url) ->
            TextButton(
                onClick = {
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    } catch (_: ActivityNotFoundException) {
                        // No browser available; ignore
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "$label\n$url",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
