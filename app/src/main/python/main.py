from yt_dlp import YoutubeDL
from requests import get
import threading
def run(search, getLength:int = 10):
    ydl_opts = {
        'skip_download': True,
        'forcetitle': True,
        'forceurl': True,
        'dump-json': True,
        'quiet': True,
        'ignoreerrors': True,
        'nocheckcertificate': True,
        'geo_bypass': True,
        'extract_flat': True,
        'playlist_count': getLength,
        'extractor_args': {'youtube': {'player_client': ['ios']}},
    }

    with YoutubeDL(ydl_opts) as ydl:
        ytlist = ydl.extract_info(f"ytsearch{getLength}:{search}",download=False)['entries']
        l = []
        for i in ytlist:
            l.append({
                'id': i['id'],
                'title': i['title'], 
                'thumbnails': i['thumbnails'], 
                'thumbnail': i['thumbnails'][0]['url'].split('.jpg')[0]+'.jpg', 
                'channel': i['channel'],
                'tags': i.get('tags', []),
                'verified': True if i.get('channel_is_verified', False) else False
                })
        print(l)
        return l

def get(id):
    option1 = {
        'quiet': True,
        'simulate': True,
        'forceurl': True,
        'format': 'bestvideo[ext=mp4]+bestaudio',
        'concurrent_fragments': 6,
        'buffer_size': 2048,
        'extract_flat': True,
        'noplaylist': True,
        "writethumbnail": False,
        'extractor_args': {'youtube': {'player_client': ['ios']}}
    }

    url = f'https://www.youtube.com/watch?v={id}'
    
    with YoutubeDL(option1) as ydl:
        info = ydl.extract_info(url, download=False)
        result={
            'id': info['id'],
            'title': info['title'], 
            'channel': info['channel'],
            'tags': info.get('tags', []),
            'thumbnail': f'https://img.youtube.com/vi/{info["id"]}/mqdefault.jpg',
            'description': info.get('description', ''),
            'verified': True if info.get('channel_is_verified', False) else False,
            'subtitles': {}
        }
        result['video'] = info['requested_formats'][0]['url']
        formats: list[dict] = info['requested_formats'][1:]
        audio = -2
        for i in formats:
            if 'audio only' in i['format']:
                print(i)
                result['audio'] = i['url']
                result['audio_type'] = i['ext']
                break
        
        s = info.get('subtitles', {})
        if (s):
            for i in s.keys():
                for l in s[i]:
                    if 'json3' == l['ext']:
                        result['subtitles'][i] = l['url']

    return result

def getRecommend():
    ydl_opts = {
        'skip_download': True,
        'forcetitle': True,
        'forceurl': True,
        'dump-json': True,
        'quiet': True,
        'ignoreerrors': True,
        'nocheckcertificate': True,
        'geo_bypass': True,
        'extract_flat': True,
        'extractor_args': {'youtube': {'player_client': ['ios']}},
        'http_headers': {'Cookie': "GPS=1;YSC=N9dfMG7iTAM;VISITOR_INFO1_LIVE=kokgJgCh25A;VISITOR_PRIVACY_METADATA=CgJLUhIEGgAgQA%3D%3D;__Secure-ROLLOUT_TOKEN=CJK0xfGZzsbC0AEQ6Iz4hdyLiwMY6Iz4hdyLiwM%3D;PREF=f4=4000000&f6=40000000&tz=Asia.Seoul&f7=100;ST-1al4flm=gs_l=youtube.3...2723.2723.0.2926......0.............1.......0...744&oq=.&itct=CA0Q7VAiEwimobSG3IuLAxW7SvUFHd7kCw8%3D&csn=P0BvYYfYIqohSA_u&endpoint=%7B%22clickTrackingParams%22%3A%22CA0Q7VAiEwimobSG3IuLAxW7SvUFHd7kCw8%3D%22%2C%22commandMetadata%22%3A%7B%22webCommandMetadata%22%3A%7B%22url%22%3A%22%2Fresults%3Fsearch_query%3D.%22%2C%22webPageType%22%3A%22WEB_PAGE_TYPE_SEARCH%22%2C%22rootVe%22%3A4724%7D%7D%2C%22searchEndpoint%22%3A%7B%22query%22%3A%22.%22%7D%7D;ST-1ykc7pc=gs_l=youtube.3...14736.15162.1.15340......0.294.1130.2-4..........4j1.........0i71k1.744&oq=.kpop&itct=CA0Q7VAiEwj8iuqL3IuLAxVBRPUFHYXWI6g%3D&csn=VaVD-DPLB6J5crso&endpoint=%7B%22clickTrackingParams%22%3A%22CA0Q7VAiEwj8iuqL3IuLAxVBRPUFHYXWI6g%3D%22%2C%22commandMetadata%22%3A%7B%22webCommandMetadata%22%3A%7B%22url%22%3A%22%2Fresults%3Fsearch_query%3D.kpop%22%2C%22webPageType%22%3A%22WEB_PAGE_TYPE_SEARCH%22%2C%22rootVe%22%3A4724%7D%7D%2C%22searchEndpoint%22%3A%7B%22query%22%3A%22.kpop%22%7D%7D;ST-10o2g84=itct=CKkCENwwGAAiEwjZobiP3IuLAxWpm1YBHSPEMp4yBnNlYXJjaFIFLmtwb3CaAQMQ9CQ%3D&csn=qosGaxLqlrS3NR9B&endpoint=%7B%22clickTrackingParams%22%3A%22CKkCENwwGAAiEwjZobiP3IuLAxWpm1YBHSPEMp4yBnNlYXJjaFIFLmtwb3CaAQMQ9CQ%3D%22%2C%22commandMetadata%22%3A%7B%22webCommandMetadata%22%3A%7B%22url%22%3A%22%2Fwatch%3Fv%3DjWQx2f-CErU%26pp%3DygUFLmtwb3A%253D%22%2C%22webPageType%22%3A%22WEB_PAGE_TYPE_WATCH%22%2C%22rootVe%22%3A3832%7D%7D%2C%22watchEndpoint%22%3A%7B%22videoId%22%3A%22jWQx2f-CErU%22%2C%22params%22%3A%22qgMFLmtwb3C6AwsI1ZT2qb7x6_jdAboDCRIHUkRBVGcwWLoDCgiixu_3k6GN_Xa6AwoIgK6Tgf2Lsr1augMLCN367-qGj86D4QG6AwoIzu3rrZW98bt2ugMKCKui5Pq7u4reEboDCgi5-_W7yIv-lhS6AwoIt_erw-LG0ZAqugMKCN-W9qb04vmMMLoDCgj09YfBs4v8n0u6AwsIm_3sxeiA_N6LAboDCwiT8orW0fmFicQBugMLCOSh5rCkuabolwG6AwsIwJiVjci3ldrKAboDCgiXkLDVp9fL2h66AwsInLDdmtCB6OCiAboDCgj-i6uF5P6jyxKCBAIQAQ%253D%253D%22%2C%22playerParams%22%3A%22ygUFLmtwb3A%253D%22%2C%22watchEndpointSupportedOnesieConfig%22%3A%7B%22html5PlaybackOnesieConfig%22%3A%7B%22commonConfig%22%3A%7B%22url%22%3A%22https%3A%2F%2Frr5---sn-n3cgv5qc5oq-20ns.googlevideo.com%2Finitplayback%3Fsource%3Dyoutube%26oeis%3D1%26c%3DWEB%26oad%3D3200%26ovd%3D3200%26oaad%3D11000%26oavd%3D11000%26ocs%3D700%26oewis%3D1%26oputc%3D1%26ofpcc%3D1%26msp%3D1%26odepv%3D1%26id%3D8d6431d9ff8212b5%26ip%3D58.234.193.85%26initcwndbps%3D4856250%26mt%3D1737630535%26oweuc%3D%26pxtags%3DCg4KAnR4Egg1MTM3MDYzMw%26rxtags%3DCg4KAnR4Egg1MTM3MDYyNQ%252CCg4KAnR4Egg1MTM3MDYyNg%252CCg4KAnR4Egg1MTM3MDYyNw%252CCg4KAnR4Egg1MTM3MDYyOA%252CCg4KAnR4Egg1MTM3MDYyOQ%252CCg4KAnR4Egg1MTM3MDYzMA%252CCg4KAnR4Egg1MTM3MDYzMQ%252CCg4KAnR4Egg1MTM3MDYzMg%252CCg4KAnR4Egg1MTM3MDYzMw%22%7D%7D%7D%7D%7D;ST-1s66z7e=gs_l=youtube.3..0i512i433k1j0i512i433i131k1l6j0i3k1j0i512i433k1j0i512k1j0i512i433i131k1j0i512i433k1j0i3k1j0i512i433i131k1.6264.7142.2.7295......0.306.888.2-1j2..........1.........0i71k1.744&oq=kpop&itct=CA0Q7VAiEwjQvLSR3IuLAxUTTw8CHbkGEoc%3D&csn=JlnHaznVk6MapbN3&endpoint=%7B%22clickTrackingParams%22%3A%22CA0Q7VAiEwjQvLSR3IuLAxUTTw8CHbkGEoc%3D%22%2C%22commandMetadata%22%3A%7B%22webCommandMetadata%22%3A%7B%22url%22%3A%22%2Fresults%3Fsearch_query%3Dkpop%22%2C%22webPageType%22%3A%22WEB_PAGE_TYPE_SEARCH%22%2C%22rootVe%22%3A4724%7D%7D%2C%22searchEndpoint%22%3A%7B%22query%22%3A%22kpop%22%7D%7D;ST-m5hzoh=itct=CJIEENwwGAAiEwjW-PSS3IuLAxWxt1YBHftCD2UyBnNlYXJjaFIEa3BvcJoBAxD0JA%3D%3D&csn=88jcsPAjD-eNqatl&endpoint=%7B%22clickTrackingParams%22%3A%22CJIEENwwGAAiEwjW-PSS3IuLAxWxt1YBHftCD2UyBnNlYXJjaFIEa3BvcJoBAxD0JA%3D%3D%22%2C%22commandMetadata%22%3A%7B%22webCommandMetadata%22%3A%7B%22url%22%3A%22%2Fwatch%3Fv%3DUTHqh7Vpa64%26pp%3DygUEa3BvcA%253D%253D%22%2C%22webPageType%22%3A%22WEB_PAGE_TYPE_WATCH%22%2C%22rootVe%22%3A3832%7D%7D%2C%22watchEndpoint%22%3A%7B%22videoId%22%3A%22UTHqh7Vpa64%22%2C%22params%22%3A%22qgMEa3BvcLoDCwjVlPapvvHr-N0BugMKCNbx7IOcuKqeRLoDCwjErob8lfXSzdYBugMKCKLG7_eToY39droDCwi1pYj8n7uMso0BugMJEgdSREFUZzBYugMKCLn79bvIi_6WFLoDCgiArpOB_YuyvVq6AwoIjafHvcjTvaV6ugMKCLSjq8PusOLPZroDCgjO7eutlb3xu3a6AwoIq6Lk-ru7it4RugMKCM_6756JjdPneroDCgi396vD4sbRkCq6AwoI2_S0oauwts8HugMKCLeq5pbVup-CWLoDCwjzxd6p3r6xmYEBugMLCICuhObPq4CSlgHyAwUNyvkfP4IEAhAB%22%2C%22playerParams%22%3A%22ygUEa3BvcA%253D%253D%22%2C%22watchEndpointSupportedOnesieConfig%22%3A%7B%22html5PlaybackOnesieConfig%22%3A%7B%22commonConfig%22%3A%7B%22url%22%3A%22https%3A%2F%2Frr3---sn-n3cgv5qc5oq-20ne.googlevideo.com%2Finitplayback%3Fsource%3Dyoutube%26oeis%3D1%26c%3DWEB%26oad%3D3200%26ovd%3D3200%26oaad%3D11000%26oavd%3D11000%26ocs%3D700%26oewis%3D1%26oputc%3D1%26ofpcc%3D1%26msp%3D1%26odepv%3D1%26id%3D5131ea87b5696bae%26ip%3D58.234.193.85%26initcwndbps%3D4405000%26mt%3D1737630535%26oweuc%3D%26pxtags%3DCg4KAnR4Egg1MTM3MDYzMw%26rxtags%3DCg4KAnR4Egg1MTM3MDYyNQ%252CCg4KAnR4Egg1MTM3MDYyNg%252CCg4KAnR4Egg1MTM3MDYyNw%252CCg4KAnR4Egg1MTM3MDYyOA%252CCg4KAnR4Egg1MTM3MDYyOQ%252CCg4KAnR4Egg1MTM3MDYzMA%252CCg4KAnR4Egg1MTM3MDYzMQ%252CCg4KAnR4Egg1MTM3MDYzMg%252CCg4KAnR4Egg1MTM3MDYzMw%22%7D%7D%7D%2C%22startTimeSeconds%22%3A0%7D%7D;CONSISTENCY=AKreu9sFnkOTzmIUdfK7ecZkmDn1mgOCbZ4hvh30bqprlugoPjsJLwNRSDQ-XziOi4ucwCgLeg1-xbd9iXct-0sEkPu88cU51fAh7r7Gv7UuVDwsBo0xGC58N88zPUU0IqCw5CC6sGETlWqiyWf4gKE;ST-1b=disableCache=true&itct=CA8QsV4iEwjW-PSS3IuLAxWxt1YBHftCD2U%3D&csn=sOysHxJjvZCh_a-X&endpoint=%7B%22clickTrackingParams%22%3A%22CA8QsV4iEwjW-PSS3IuLAxWxt1YBHftCD2U%3D%22%2C%22commandMetadata%22%3A%7B%22webCommandMetadata%22%3A%7B%22url%22%3A%22%2F%22%2C%22webPageType%22%3A%22WEB_PAGE_TYPE_BROWSE%22%2C%22rootVe%22%3A3854%2C%22apiUrl%22%3A%22%2Fyoutubei%2Fv1%2Fbrowse%22%7D%7D%2C%22browseEndpoint%22%3A%7B%22browseId%22%3A%22FEwhat_to_watch%22%7D%7D"}

    }
    with YoutubeDL(ydl_opts) as ydl:
        info = ydl.extract_info("ytsearch:-v https://www.youtube.com", download=False)
        print(info)

def getPlaylist(url: str):
    ydl_opts = {
        'skip_download': True,
        'forcetitle': True,
        'forceurl': True,
        'quiet': True,
        'ignoreerrors': True,
        'simulate': True,
        'forceurl': True,
        'buffer_size': 2048,
        'extract_flat': True,
        'extractor_args': {'youtube': {'player_client': ['ios']}}
    }
    with YoutubeDL(ydl_opts) as ydl:
        data = ydl.extract_info(url, download=False)
    result = {
        'title': data['title'],
        'id': data['id'],
        'uploader': data['uploader'],
        'description': data['description'],
        'items': []
    }
    for i in data.keys():
        if i == 'entries': continue
        print(i,data[i])
    print('\n\n#######################\n\n')
    for i in data['entries']:
        result['items'].append(
            {
                'title': i['title'],
                'channel': i.get('channel', ''),
                'thumbnail': f'https://img.youtube.com/vi/{i["id"]}/mqdefault.jpg',
                'id': i['id']
            }
        )
    print(data.keys())
    return result
length = 0
def getPlaylistVideo(videoId: str):
    global length
    ids = videoId[:-1].split(',')
    threads = []
    length = 0
    result = {'length':len(ids)}
    failure = []
    def load(index, i):
        global length
        try:
            result[str(index)] = get(i)
        except: failure.append(index)
        length+=1
    for index, i in enumerate(ids):
        try:
            threads.append(
                threading.Thread(target=load,args=[index,i,])
            )
            threads[-1].start()
        except: pass
    
    for i in threads: i.join()
    result['fail'] = failure
    print(result['length'], length, failure)
    return result

temp = '''
id PL_D2YrKeYY2UvRIw_SW3lQXzBDO7aBeii
title COVER
availability None
channel_follower_count None
description
tags []
thumbnails [{'url': 'https://i.ytimg.com/vi/EtkolJZEmpg/hqdefault.jpg?sqp=-oaymwEWCKgBEF5IWvKriqkDCQgBFQAAiEIYAQ==&rs=AOn4CLAzQ5hU6qByDnhYl1YToZ-dz5kh2g', 'height': 94, 'width': 168, 'id': '0', 'resolution': '168x94'}, {'url': 'https://i.ytimg.com/vi/EtkolJZEmpg/hqdefault.jpg?sqp=-oaymwEWCMQBEG5IWvKriqkDCQgBFQAAiEIYAQ==&rs=AOn4CLATjCPfI5mjHU2xqC8JDtsKCSuOVw', 'height': 110, 'width': 196, 'id': '1', 'resolution': '196x110'}, {'url': 'https://i.ytimg.com/vi/EtkolJZEmpg/hqdefault.jpg?sqp=-oaymwEXCPYBEIoBSFryq4qpAwkIARUAAIhCGAE=&rs=AOn4CLAL_qvLmso8aeTPQ44cT-lK89vRhQ', 'height': 138, 'width': 246, 'id': '2', 'resolution': '246x138'}, {'url': 'https://i.ytimg.com/vi/EtkolJZEmpg/hqdefault.jpg?sqp=-oaymwEXCNACELwBSFryq4qpAwkIARUAAIhCGAE=&rs=AOn4CLCA8hNHszJAGsTvkndOWAt8nGJkSw', 'height': 188, 'width': 336, 'id': '3', 'resolution': '336x188'}]
modified_date 20250109
view_count 97767
playlist_count 11
channel 유즈하 리코 YUZUHA RIKO
channel_id UCj0c1jUr91dTetIQP2pFeLA
uploader_id @yuzuhariko
uploader 유즈하 리코 YUZUHA RIKO
channel_url https://www.youtube.com/channel/UCj0c1jUr91dTetIQP2pFeLA
uploader_url https://www.youtube.com/@yuzuhariko
_type playlist
extractor_key YoutubeTab
extractor youtube:tab
webpage_url https://www.youtube.com/playlist?list=PL_D2YrKeYY2UvRIw_SW3lQXzBDO7aBeii
original_url https://www.youtube.com/playlist?list=PL_D2YrKeYY2UvRIw_SW3lQXzBDO7aBeii
webpage_url_basename playlist
webpage_url_domain youtube.com
release_year None
epoch 1737817894

'''


temp = '''
dict_keys(['id', 'title', 'formats', 'thumbnails', 'thumbnail', 'description',
 'channel_id', 'channel_url', 'duration', 'view_count', 'average_rating',
  'age_limit', 'webpage_url', 'categories', 'tags', 'playable_in_embed',
   'live_status', 'release_timestamp', '_format_sort_fields', 'automatic_captions', 
   'subtitles', 'comment_count', 'chapters', 'heatmap', 'like_count', 'channel', 
   'channel_follower_count', 'channel_is_verified', 'uploader', 'uploader_id', 
   'uploader_url', 'upload_date', 'timestamp', 'availability', 'original_url', 
   'webpage_url_basename', 'webpage_url_domain', 'extractor', 'extractor_key', 
   'playlist_count', 'playlist', 'playlist_id', 'playlist_title', 'playlist_uploader',
    'playlist_uploader_id', 'playlist_channel', 'playlist_channel_id', 
    'playlist_webpage_url', 'n_entries', 'playlist_index', '__last_playlist_index',
     'playlist_autonumber', 'display_id', 'fulltitle', 'duration_string', 
     'release_year', 'is_live', 'was_live', 'requested_subtitles', '_has_drm', 'epoch', 
     'asr', 'filesize', 'format_id', 'format_note', 'source_preference', 'fps', 
     'audio_channels', 'height', 'quality', 'has_drm', 'tbr', 'filesize_approx', 'url',
      'width', 'language', 'language_preference', 'preference', 'ext', 'vcodec', 'acodec',
       'dynamic_range', 'downloader_options', 'protocol', 'video_ext', 'audio_ext', 'vbr',
        'abr', 'resolution', 'aspect_ratio', 'http_headers', 'format'])
'''


if __name__ == '__main__':
    ids = ''
    for i in getPlaylist('https://www.youtube.com/playlist?list=PLK-R3JpdSAC_rjQMmdd1OA5ns5g9UIqT9')['items']:
        ids+=i['id']+','
    s = getPlaylistVideo(ids)#'https://www.youtube.com/playlist?list=PL_D2YrKeYY2UvRIw_SW3lQXzBDO7aBeii') #['en', 'ja', 'ko']
    #print(s)

#'thumbnails': [{'url': 'https://i.ytimg.com/vi/DK0c-Ka4AyM/hq720.jpg?sqp=-oaymwEcCOgCEMoBSFXyq4qpAw4IARUAAIhCGAFwAcABBg==&rs=AOn4CLDFSCIMRhCUgd-dgz79Gh16XeyfTA', 'height': 202, 'width': 360}, {'url': 'https://i.ytimg.com/vi/DK0c-Ka4AyM/hq720.jpg?sqp=-oaymwEcCNAFEJQDSFXyq4qpAw4IARUAAIhCGAFwAcABBg==&rs=AOn4CLDQRIuKo6IRtzL4Cx2Bq6MTyegexw', 'height': 404, 'width': 720}], 'channel': 'Pororo Korean Official'}, {'id': 'WHsD6yu-QUI', 'title': '초등학생들에게 추천하는 동요 모음', 'thumbnails': [{'url': 'https://i.ytimg.com/vi/WHsD6yu-QUI/hq720.jpg?sqp=-oaymwEcCOgCEMoBSFXyq4qpAw4IARUAAIhCGAFwAcABBg==&rs=AOn4CLAbeJoDqJHAa-VOd19gJcB9G1_7Tw', 'height': 202, 'width': 360}, {'url': 'https://i.ytimg.com/vi/WHsD6yu-QUI/hq720.jpg?sqp=-oaymwEcCNAFEJQDSFXyq4qpAw4IARUAAIhCGAFwAcABBg==&rs=AOn4CLD8jYQr3ucraEHBGr-u4JvONhDn8A', 'height': 404, 'width': 720}]