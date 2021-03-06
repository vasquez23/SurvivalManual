package org.ligi.survivalmanual.adapter

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.QuoteSpan
import android.text.style.URLSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.github.rjeschke.txtmark.Processor
import org.ligi.compat.HtmlCompat
import org.ligi.survivalmanual.R
import org.ligi.survivalmanual.functions.highLight
import org.ligi.survivalmanual.functions.linkImagesInMarkDown
import org.ligi.survivalmanual.model.State
import org.ligi.survivalmanual.ui.CustomQuoteSpan
import org.ligi.survivalmanual.viewholder.TextContentViewHolder

class MarkdownRecyclerAdapter(val list: List<String>, val imageWidth: Int, val onURLClick: (url: String) -> Unit) : RecyclerView.Adapter<TextContentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextContentViewHolder {
        val textView = LayoutInflater.from(parent.context).inflate(R.layout.text, parent, false) as TextView
        textView.movementMethod = LinkMovementMethod.getInstance()
        return TextContentViewHolder(textView)
    }

    override fun onBindViewHolder(holder: TextContentViewHolder, position: Int) {

        val html = Processor.process(linkImagesInMarkDown( if (State.searchTerm.isNullOrEmpty()) {
            list[position]
        }else {
            highLight(list[position],State.searchTerm)
        }))

        setTextViewHTML(holder.view, html)
    }

    override fun getItemCount() = list.size

    private fun makeLinkClickable(strBuilder: SpannableStringBuilder, span: URLSpan) {
        val start = strBuilder.getSpanStart(span)
        val end = strBuilder.getSpanEnd(span)
        val flags = strBuilder.getSpanFlags(span)
        val clickable = object : ClickableSpan() {
            override fun onClick(view: View) {
                onURLClick(span.url)
            }
        }
        strBuilder.setSpan(clickable, start, end, flags)
        strBuilder.removeSpan(span)
    }

    private fun replaceQuoteSpans(ctx: Context, spannable: Spannable) {
        val quoteSpans = spannable.getSpans(0, spannable.length, QuoteSpan::class.java)
        for (quoteSpan in quoteSpans) {
            val start = spannable.getSpanStart(quoteSpan)
            val end = spannable.getSpanEnd(quoteSpan)
            val flags = spannable.getSpanFlags(quoteSpan)
            spannable.removeSpan(quoteSpan)
            spannable.setSpan(CustomQuoteSpan(ctx), start, end, flags)
        }
    }

    private fun setTextViewHTML(text: TextView, html: String, ctx: Context = text.context) {

        text.textSize = State.getFontSize()
        class CustomImageGetter : Html.ImageGetter {
            override fun getDrawable(source: String?): Drawable {

                val bitmapDrawable = BitmapDrawable.createFromStream(ctx.assets.open("md/" + source), source) as BitmapDrawable

                val ratio = bitmapDrawable.bitmap.height.toFloat() / bitmapDrawable.bitmap.width

                bitmapDrawable.setBounds(0, 0, imageWidth, (imageWidth * ratio).toInt())
                return bitmapDrawable
            }

        }

        val sequence = HtmlCompat.fromHtml(html, CustomImageGetter(), null)
        val spannable = SpannableStringBuilder(sequence)
        val urls = spannable.getSpans(0, sequence.length, URLSpan::class.java)

        urls.forEach { makeLinkClickable(spannable, it) }

        replaceQuoteSpans(ctx, spannable)
        text.text = spannable
        text.movementMethod = LinkMovementMethod.getInstance()
    }

}